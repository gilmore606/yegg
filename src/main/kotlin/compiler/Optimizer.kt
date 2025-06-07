package com.dlfsystems.compiler

import com.dlfsystems.value.Value
import com.dlfsystems.vm.Executable
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.VMWord

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
            // If we've reached a block boundary, record its new address
            coder.blocks.forEachIndexed { n, it ->
                if (it.start == pc) blockStarts[n] = outMem.size
                if (it.end == pc) blockEnds[n] = outMem.size
            }

            // Optimizations

            consume(O_NEGATE, O_NEGATE)?.also { }

            ?: consume(O_SETVAR, null, O_GETVAR, null) { args ->
                args[0].isInt() && args[1].isInt(args[0].intFromV)
            }?.also { args ->
                code(O_SETGETVAR)
                value(args[0].value!!)
            }

            ?: consume(O_VAL, null, O_CMP_EQ) { args -> args[0].isInt(0) }?.also { code(O_CMP_EQZ) }
            ?: consume(O_VAL, null, O_CMP_GT) { args -> args[0].isInt(0) }?.also { code(O_CMP_GTZ) }
            ?: consume(O_VAL, null, O_CMP_GE) { args -> args[0].isInt(0) }?.also { code(O_CMP_GEZ) }
            ?: consume(O_VAL, null, O_CMP_LT) { args -> args[0].isInt(0) }?.also { code(O_CMP_LTZ) }
            ?: consume(O_VAL, null, O_CMP_LE) { args -> args[0].isInt(0) }?.also { code(O_CMP_LEZ) }

            ?: consume(O_GETVAR, null, O_CMP_EQ, O_IF, null)?.also { args ->
                code(O_IFVAREQ)
                value(args[0].value!!)
                address(args[1].address!!)
            }

            ?: consume(O_VAL, null, O_RETURN)?.also { args ->
                code(O_RETVAL)
                value(args[0].value!!)
            }

            ?: consume(O_GETVAR, null, O_RETURN)?.also { args ->
                code(O_RETVAR)
                value(args[0].value!!)
            }

            ?: consume(O_ADD, O_VAL, null, O_ADD)?.also { args ->
                code(O_CONCAT)
                value(args[0].value!!)
            }

            ?: consume(O_VAL, null, O_ADD)?.also { args ->
                code(O_ADDVAL)
                value(args[0].value!!)
            }

            ?: consume(O_FUNCALL, null, null, O_DISCARD)?.also { args ->
                code(O_FUNCALLST)
                value(args[0].value!!)
                value(args[1].value!!)
            }

            ?: consume(O_PASS, null, O_DISCARD)?.also { args ->
                code(O_PASSST)
                value(args[0].value!!)
            }

            ?: consume(O_VAL, null, O_CALL, null, O_DISCARD)?.also { args ->
                code(O_VCALLST)
                value(args[1].value!!) // write O_CALL arg first
                value(args[0].value!!)
            }

            ?: consume(O_VAL, null, O_CALL, null)?.also { args ->
                code(O_VCALL)
                value(args[1].value!!) // write O_CALL arg first
                value(args[0].value!!)
            }

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
        val alreadyFilled = mutableSetOf<Int>()
        jumpMap.keys.forEach { old ->
            outMem.forEachIndexed { pc, word ->
                if (!alreadyFilled.contains(pc)) {
                    if (word.address == old) {
                        word.address = jumpMap[old]
                        alreadyFilled.add(pc)
                    }
                }
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
            else if (mem[pc + i].opcode != t) hit = false
            // Miss if we're going to wipe out a jump dest
            else if (i > 0 && ((pc + i) in jumpMap.keys)) hit = false
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
