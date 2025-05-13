package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.server.Yegg
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.VMWord
import com.dlfsystems.value.*
import com.dlfsystems.vm.Executable
import com.dlfsystems.world.ObjID

class Coder(val ast: Node) {

    var mem = mutableListOf<VMWord>()

    // Addresses to be filled in with a named jump point once coded.
    val forwardJumps = HashMap<String, MutableSet<Int>>()
    // Addresses stored to be used as future jump destinations.
    val backJumps = HashMap<String, Int>()
    // Entry points to literal VFuns.
    val blocks = mutableListOf<Executable.Block>()

    fun last() = if (mem.isEmpty()) null else mem[mem.size - 1]

    // Compile the AST into a list of opcodes, by recursively asking the nodes to code themselves.
    // Nodes will then call Coder.code() and Coder.value() to output their compiled code.
    fun generate() {
        ast.code(this)
        if (Yegg.optimizeCompiler) Optimizer(this).optimize()
    }

    // Write an opcode into memory.
    fun code(from: Node, op: Opcode) {
        mem.add(VMWord(from.lineNum, from.charNum, opcode = op))
    }

    // Write a Value into memory, as an argument to the previous opcode.
    fun value(from: Node, value: Value) {
        mem.add(VMWord(from.lineNum, from.charNum, value = value))
    }
    fun value(from: Node, intValue: Int) { value(from, VInt(intValue)) }
    fun value(from: Node, boolValue: Boolean) { value(from, VBool(boolValue)) }
    fun value(from: Node, floatValue: Float) { value(from, VFloat(floatValue)) }
    fun value(from: Node, stringValue: String) { value(from, VString(stringValue)) }
    fun value(from: Node, objValue: ObjID) { value(from, VObj(objValue)) }
    fun value(from: Node, listValue: List<Value>) { value(from, VList(listValue.toMutableList())) }
    fun value(from: Node, mapValue: Map<Value, Value>) { value(from, VMap(mapValue.toMutableMap())) }

    // Record a block start address of a literal VFun.
    // Code the block index (as arg for O_FUNVAL).
    // Return the index to be passed later to blockEnd.
    fun codeBlockStart(from: Node): Int {
        val entryPoint = blocks.size
        value(from, entryPoint)
        blocks.add(Executable.Block(mem.size + 2, -1))  // +2 to skip the O_JUMP<addr>
        return entryPoint
    }

    // Record a block end address.  Assumes the start was already coded!
    fun codeBlockEnd(from: Node, block: Int) {
        blocks[block] = Executable.Block(
            blocks[block].start, mem.size
        )
    }

    // Write a placeholder address for a jump we'll locate in the future.
    // Nodes call this to jump to a named future address.
    fun jumpForward(from: Node, name: String) {
        val address = mem.size
        val fullname = "$name${from.id}"
        if (forwardJumps.containsKey(fullname)) {
            forwardJumps[fullname]!!.add(address)
        } else {
            forwardJumps[fullname] = mutableSetOf(address)
        }
        mem.add(VMWord(from.lineNum, from.charNum, address = -1))
    }

    // Reach a previously named jump point.  Fill in all previous references with the current address.
    // Nodes call this when a previously named jumpForward address is reached.
    fun setForwardJump(from: Node, name: String) {
        val dest = mem.size
        val fullname = "$name${from.id}"
        forwardJumps[fullname]!!.forEach { loc ->
            mem[loc].fillAddress(dest)
        }
        forwardJumps.remove(fullname)
    }

    // Record a jump address we'll jump back to later.
    // Nodes call this to mark a named address which they'll code a jumpBack to.
    fun setBackJump(from: Node, name: String) {
        val dest = mem.size
        val fullname = "$name${from.id}"
        backJumps[fullname] = dest
    }

    // Write address of a jump located in the past.
    // Nodes call this to jump to a named past address.
    fun jumpBack(from: Node, name: String) {
        val fullname = "$name${from.id}"
        val dest = backJumps[fullname]
        mem.add(VMWord(from.lineNum, from.charNum, address = dest))
    }


    class Optimizer(private val coder: Coder) {
        private val mem = coder.mem
        private val outMem = mutableListOf<VMWord>()
        private val jumpMap = mutableMapOf<Int, Int>()
        private val blockStarts = mutableListOf<Int>()
        private val blockEnds = mutableListOf<Int>()
        private var pc = 0
        private var lastMatchSize = 0

        fun optimize() {

            // Find all jump destinations in source
            mem.forEach { word ->
                word.address?.also { address ->
                    jumpMap[address] = -1
                }
            }
            blockStarts.addAll(coder.blocks.map { it.start })
            blockEnds.addAll(coder.blocks.map { it.end })

            pc = 0
            while (pc < mem.size) {
                // If we've reached a jump dest, record its new address
                if (jumpMap.containsKey(pc)) jumpMap[pc] = outMem.size
                // If we've reached an entry point, record its new address
                coder.blocks.forEachIndexed { n, it ->
                    if (it.start == pc) blockStarts[n] = outMem.size
                    if (it.end == pc) blockEnds[n] = outMem.size
                }

                // NEGATE NEGATE => ()
                consume(O_NEGATE, O_NEGATE)?.also { }

                // SETVAR GETVAR => SETGETVAR
                ?: consume(O_SETVAR, null, O_GETVAR, null) { args ->
                    args[0].isInt() && args[1].isInt(args[0].intFromV)
                }?.also { args ->
                    code(O_SETGETVAR)
                    value(args[0].value!!)
                }

                // O_VAL 0 O_CMP_xx => O_CMP_xxZ
                ?: consume(O_VAL, null, O_CMP_EQ) { args -> args[0].isInt(0) }?.also { code(O_CMP_EQZ) }
                ?: consume(O_VAL, null, O_CMP_GT) { args -> args[0].isInt(0) }?.also { code(O_CMP_GTZ) }
                ?: consume(O_VAL, null, O_CMP_GE) { args -> args[0].isInt(0) }?.also { code(O_CMP_GEZ) }
                ?: consume(O_VAL, null, O_CMP_LT) { args -> args[0].isInt(0) }?.also { code(O_CMP_LTZ) }
                ?: consume(O_VAL, null, O_CMP_LE) { args -> args[0].isInt(0) }?.also { code(O_CMP_LEZ) }

                // O_GETVAR O_CMP_EQ O_IF => O_IFVAREQ
                ?: consume(O_GETVAR, null, O_CMP_EQ, O_IF, null)?.also { args ->
                    code(O_IFVAREQ)
                    value(args[0].value!!)
                    address(args[1].address!!)
                }

                // O_VAL O_RETURN => O_RETVAL
                ?: consume(O_VAL, null, O_RETURN)?.also { args ->
                    code(O_RETVAL)
                    value(args[0].value!!)
                }

                // O_GETVAR O_RETURN => O_RETVAR
                ?: consume(O_GETVAR, null, O_RETURN)?.also { args ->
                    code(O_RETVAR)
                    value(args[0].value!!)
                }

                // O_ADD O_VAL O_ADD => O_CONCAT
                ?: consume(O_ADD, O_VAL, null, O_ADD)?.also { args ->
                    code(O_CONCAT)
                    value(args[0].value!!)
                }

                // O_VAL O_ADD => O_ADDVAL
                ?: consume(O_VAL, null, O_ADD)?.also { args ->
                    code(O_ADDVAL)
                    value(args[0].value!!)
                }

                // O_FUNCALL O_DISCARD => O_FUNVOKE
                ?: consume(O_FUNCALL, null, null, O_DISCARD)?.also { args ->
                    code(O_FUNCALLST)
                    value(args[0].value!!)
                    value(args[1].value!!)
                }

                // O_VAL O_CALL O_DISCARD => O_VCVOKE
                ?: consume(O_VAL, null, O_CALL, null, O_DISCARD)?.also { args ->
                    code(O_VCALLST)
                    value(args[1].value!!) // write O_CALL arg first
                    value(args[0].value!!)
                }

                // O_VAL O_CALL => O_VCALL
                ?: consume(O_VAL, null, O_CALL, null)?.also { args ->
                    code(O_VCALL)
                    value(args[1].value!!) // write O_CALL arg first
                    value(args[0].value!!)
                }

                // O_VAL O_GETPROP => O_VGETPROP
                ?: consume(O_VAL, null, O_GETPROP)?.also { args ->
                    code(O_VGETPROP)
                    value(args[0].value!!)
                }

                ?: consume(O_VAL, null, O_TRAIT)?.also { args ->
                    code(O_VTRAIT)
                    value(args[0].value!!)
                }


                // If nothing matched, copy and continue
                ?: run {
                    outMem.add(mem[pc++])
                }
            }

            // Replace all jump dests
            jumpMap.keys.forEach { old ->
                outMem.forEach { word ->
                    if (word.address == old) word.address = jumpMap[old]
                }
            }
            // Replace all block addresses
            coder.blocks.clear()
            coder.blocks.addAll(blockStarts.mapIndexed { n, it ->
                Executable.Block(it, blockEnds[n])
            })
            // Replace compiled code
            coder.mem = outMem
        }

        // Match and consume a series of opcodes (or null for any non-opcode word).
        private fun consume(vararg opcodes: Opcode?, check: ((List<VMWord>)->Boolean)? = null): List<VMWord>? {
            if (opcodes.size > (mem.size - pc)) return null
            var hit = true
            val nulls = mutableListOf<VMWord>()
            opcodes.forEachIndexed { i, t ->
                if (t == null) nulls.add(mem[pc + i])
                else if ((pc + i) in jumpMap.keys) hit = false  // Miss if we overlap a jump dest
                else if (mem[pc + i].opcode != t) hit = false
            }
            if (hit && (check?.invoke(nulls) != false)) {
                pc += opcodes.size
                lastMatchSize = opcodes.size
                return nulls
            }
            return null
        }

        private fun code(op: Opcode) {
            val oldword = mem[pc - lastMatchSize]
            outMem.add(VMWord(oldword.lineNum, oldword.charNum, op))
        }
        private fun value(v: Value) {
            val oldword = mem[pc - lastMatchSize]
            outMem.add(VMWord(oldword.lineNum, oldword.charNum, value = v))
        }
        private fun address(a: Int) {
            val oldword = mem[pc - lastMatchSize]
            outMem.add(VMWord(oldword.lineNum, oldword.charNum, address = a))
        }
    }
}
