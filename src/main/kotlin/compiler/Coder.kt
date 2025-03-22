package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.VMWord
import com.dlfsystems.value.*
import java.util.UUID

class Coder(val ast: Node) {

    var mem = ArrayList<VMWord>()

    // Addresses to be filled in with a named jump point once coded.
    val forwardJumps = HashMap<String, MutableSet<Int>>()
    // Addresses stored to be used as future jump destinations.
    val backJumps = HashMap<String, Int>()

    fun last() = if (mem.isEmpty()) null else mem[mem.size - 1]

    // Compile the AST into a list of opcodes, by recursively asking the nodes to code themselves.
    // Nodes will then call Coder.code() and Coder.value() to output their compiled code.
    fun generate() {
        ast.code(this)
    }

    fun postOptimize() {
        Optimizer.postOptimize(mem)
        mem = Optimizer.mem
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
    fun value(from: Node, objValue: UUID) { value(from, VObj(objValue)) }

    // Write a placeholder address for a jump we'll locate in the future.
    // Nodes call this to jump to a named future address.
    fun jumpForward(from: Node, name: String) {
        val address = mem.size
        if (forwardJumps.containsKey(name)) {
            forwardJumps[name]!!.add(address)
        } else {
            forwardJumps[name] = mutableSetOf(address)
        }
        mem.add(VMWord(from.lineNum, from.charNum, address = -1))
    }

    // Reach a previously named jump point.  Fill in all previous references with the current address.
    // Nodes call this when a previously named jumpForward address is reached.
    fun setForwardJump(from: Node, name: String) {
        val dest = mem.size
        forwardJumps[name]!!.forEach { loc ->
            mem[loc].fillAddress(dest)
        }
        forwardJumps.remove(name)
    }

    // Record a jump address we'll jump back to later.
    // Nodes call this to mark a named address which they'll code a jumpBack to.
    fun setBackJump(from: Node, name: String) {
        val dest = mem.size
        backJumps[name] = dest
    }

    // Write address of a jump located in the past.
    // Nodes call this to jump to a named past address.
    fun jumpBack(from: Node, name: String) {
        val dest = backJumps[name]
        mem.add(VMWord(from.lineNum, from.charNum, address = dest))
    }


    object Optimizer {
        var source = ArrayList<VMWord>()
        var mem = ArrayList<VMWord>()
        val jumpMap = HashMap<Int, Int>()
        var pc = 0
        var lastMatchSize = 0

        fun postOptimize(withSource: ArrayList<VMWord>) {

            // Find all jump destinations in source
            source = withSource
            source.forEach { word ->
                word.address?.also { address ->
                    jumpMap[address] = -1
                }
            }

            mem.clear()
            pc = 0
            while (pc < source.size) {
                // If we've reached a jump dest, record its new address
                if (jumpMap.containsKey(pc)) jumpMap[pc] = mem.size


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


                // If nothing matched, copy and continue
                ?: run {
                    mem.add(source[pc++])
                }
            }

            // Replace all jump dests
            jumpMap.keys.forEach { old ->
                mem.forEach { word ->
                    if (word.address == old) word.address = jumpMap[old]
                }
            }
        }

        // Match and consume a series of opcodes (or null for any non-opcode word).
        private fun consume(vararg opcodes: Opcode?, check: ((List<VMWord>)->Boolean)? = null): List<VMWord>? {
            if (opcodes.size > (source.size - pc)) return null
            var hit = true
            var nulls = mutableListOf<VMWord>()
            opcodes.forEachIndexed { i, t ->
                if (t == null) nulls.add(source[pc + i])
                else if ((pc + i) in jumpMap.keys) hit = false  // Miss if we overlap a jump dest
                else if (source[pc + i].opcode != t) hit = false
            }
            if (hit && (check?.invoke(nulls) != false)) {
                pc += opcodes.size
                lastMatchSize = opcodes.size
                return nulls
            }
            return null
        }

        private fun code(op: Opcode) {
            val oldword = source[pc - lastMatchSize]
            mem.add(VMWord(oldword.lineNum, oldword.charNum, op))
        }
        private fun value(v: Value) {
            val oldword = source[pc - lastMatchSize]
            mem.add(VMWord(oldword.lineNum, oldword.charNum, value = v))
        }
    }
}
