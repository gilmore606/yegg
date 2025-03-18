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
        var pc = 0
        var mem = ArrayList<VMWord>()
        var source = ArrayList<VMWord>()

        fun postOptimize(withSource: ArrayList<VMWord>) {
            source = withSource
            mem.clear()
            pc = 0
            while (pc < source.size) {

                // Delete echoed O_NEGATEs
                consume(O_NEGATE, O_NEGATE)?.also { }

                // Combine SETVAR n, GETVAR n to SETGETVAR n
                consume(O_SETVAR, null, O_GETVAR, null) { nulls ->
                    nulls[0].intFromV == nulls[1].intFromV
                }?.also { nulls ->
                    code(O_SETGETVAR)
                    value(nulls[0].value!!)
                }

                pc++
            }
        }

        // Match and consume a series of opcodes (or null for any non-opcode word).
        private fun consume(vararg opcodes: Opcode?, check: ((List<VMWord>)->Boolean)? = null): List<VMWord>? {
            if (opcodes.size > (source.size - pc)) return null
            var hit = true
            var nulls = mutableListOf<VMWord>()
            opcodes.forEachIndexed { i, t ->
                if (t == null) nulls.add(source[pc + i])
                else if (source[pc + i].opcode != t) hit = false
            }
            if (hit && (check?.invoke(nulls) != false)) {
                pc += opcodes.size
                return source.subList(pc - opcodes.size, pc)
            }
            return null
        }

        // TODO: preserve line+char!
        private fun code(op: Opcode) {
            mem.add(VMWord(0, 0, op))
        }
        private fun value(v: Value) {
            mem.add(VMWord(0, 0, value = v))
        }
    }


    fun dumpText(): String {
        var s = ""
        var pc = 0
        while (pc < mem.size) {
            val cell = mem[pc]
            s += "<$pc> "
            s += cell.toString()
            cell.opcode?.also { opcode ->
                repeat (opcode.argCount) {
                    pc++
                    val arg = mem[pc]
                    s += " $arg"
                }
            }
            s += "\n"
            pc++
        }
        return s
    }
}
