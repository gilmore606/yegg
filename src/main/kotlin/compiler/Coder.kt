package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.VMWord
import com.dlfsystems.vm.Value

class Coder(val ast: Node) {

    val mem = ArrayList<VMWord>()
    val futureJumps = HashMap<String, MutableSet<Int>>()
    val pastJumps = HashMap<String, Int>()

    fun last() = if (mem.isEmpty()) null else mem[mem.size - 1]

    // Compile the AST into a list of opcodes, by recursively asking the nodes to code themselves.
    // Nodes will then call Coder.code() and Coder.value() to output their compiled code.
    fun generate() {
        ast.code(this)
    }

    // Write an opcode into memory.
    fun code(from: Node, op: Opcode) {
        // optimization: remove doubled O_NEGATE
        if (op == O_NEGATE && last()?.opcode == O_NEGATE) {
            mem.removeLast()
            return
        }
        mem.add(VMWord(from.lineNum, from.charNum, opcode = op))
    }

    // Write a Value into memory, as an argument to the previous opcode.
    fun value(from: Node, value: Value) {
        mem.add(VMWord(from.lineNum, from.charNum, value = value))
    }
    fun value(from: Node, intValue: Int) {
        value(from, Value.VInt(intValue))
    }
    fun value(from: Node, boolValue: Boolean) {
        value(from, Value.VBool(boolValue))
    }
    fun value(from: Node, floatValue: Float) {
        value(from, Value.VFloat(floatValue))
    }
    fun value(from: Node, stringValue: String) {
        value(from, Value.VString(stringValue))
    }

    // Write a placeholder address for a jump we'll locate in the future.
    // Nodes call this to jump to a named future address.
    fun jumpFuture(from: Node, name: String) {
        val address = mem.size
        if (futureJumps.containsKey(name)) {
            futureJumps[name]!!.add(address)
        } else {
            futureJumps[name] = mutableSetOf(address)
        }
        mem.add(VMWord(from.lineNum, from.charNum, address = -1))
    }

    // Reach a previously named jump point.  Fill in all previous references with the current address.
    // Nodes call this when a previously named jumpFuture address is reached.
    fun reachFuture(from: Node, name: String) {
        val dest = mem.size
        futureJumps[name]!!.forEach { loc ->
            mem[loc].fillAddress(dest)
        }
        futureJumps.remove(name)
    }

    // Record a jump address we'll jump back to later.
    // Nodes call this to mark a named address which they'll jumpPast back to.
    fun reachPast(from: Node, name: String) {
        val dest = mem.size
        pastJumps[name] = dest
    }

    // Write address of a jump located in the past.
    // Nodes call this to jump to a named past address.
    fun jumpPast(from: Node, name: String) {
        val dest = pastJumps[name]
        mem.add(VMWord(from.lineNum, from.charNum, address = dest))
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
