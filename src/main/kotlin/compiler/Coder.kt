package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.server.Yegg
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.VMWord
import com.dlfsystems.value.*
import com.dlfsystems.vm.Executable
import com.dlfsystems.world.Obj

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
        if (Yegg.conf.optimizeCompiler) Optimizer(this).optimize()
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
    fun value(from: Node, objValue: Obj.ID) { value(from, VObj(objValue)) }
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

}
