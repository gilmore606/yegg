package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.VMCell
import com.dlfsystems.vm.Value

class Coder(val ast: Node) {

    val mem = ArrayList<VMCell>()

    fun generate() {
        ast.code(this)
    }

    fun code(from: Node, op: Opcode) {
        mem.add(VMCell(from.lineNum, from.charNum, opcode = op))
    }

    fun value(from: Node, value: Value) {
        mem.add(VMCell(from.lineNum, from.charNum, value = value))
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
