package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.VMCell
import com.dlfsystems.vm.Value

class Coder(val ast: Node) {

    val mem = ArrayList<VMCell>()
    val futureJumps = HashMap<String, MutableSet<Int>>()
    val pastJumps = HashMap<String, Int>()

    fun last() = if (mem.isEmpty()) null else mem[mem.size - 1]


    fun generate() {
        ast.code(this)
    }

    fun code(from: Node, op: Opcode) {
        if (op == O_NEGATE && last()?.opcode == O_NEGATE) {
            mem.removeLast()
            return
        }
        mem.add(VMCell(from.lineNum, from.charNum, opcode = op))
    }

    fun value(from: Node, value: Value) {
        mem.add(VMCell(from.lineNum, from.charNum, value = value))
    }

    // Write a placeholder address for a jump we'll locate in the future.
    fun jumpFuture(from: Node, name: String) {
        val address = mem.size
        if (futureJumps.containsKey(name)) {
            futureJumps[name]!!.add(address)
        } else {
            futureJumps[name] = mutableSetOf(address)
        }
        mem.add(VMCell(from.lineNum, from.charNum, address = -1))
    }

    // Reach a previously named jump point.  Fill in all previous references with the current address.
    fun reachFuture(from: Node, name: String) {
        val dest = mem.size
        futureJumps[name]!!.forEach { loc ->
            mem[loc].fillAddress(dest)
        }
        futureJumps.remove(name)
    }

    // Record a jump address we'll jump back to later.
    fun reachPast(from: Node, name: String) {
        val dest = mem.size
        pastJumps[name] = dest
    }

    // Write address of a jump located in the past.
    fun jumpPast(from: Node, name: String) {
        val dest = pastJumps[name]
        mem.add(VMCell(from.lineNum, from.charNum, address = dest))
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
