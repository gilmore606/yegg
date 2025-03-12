package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.VMCell
import com.dlfsystems.vm.Value

class Coder(val ast: Node) {

    val mem = ArrayList<VMCell>()
    val futureJumps = HashMap<String, MutableSet<Int>>()

    fun generate() {
        ast.code(this)
    }

    fun code(from: Node, op: Opcode) {
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

    fun dumpText(): String {
        var s = ""
        var pc = 0
        while (pc < mem.size) {
            val cell = mem[pc]
            s += cell.toString()
            if (cell.isOpcode()) {
                repeat (cell.opcode!!.argCount) {
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
