package com.dlfsystems.yegg.compiler

import com.dlfsystems.yegg.compiler.ast.Node
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.vm.Opcode
import com.dlfsystems.yegg.vm.VMWord
import com.dlfsystems.yegg.value.*
import com.dlfsystems.yegg.vm.Executable
import com.dlfsystems.yegg.vm.VMException
import com.dlfsystems.yegg.world.Obj

// Generate compiled bytecode by traversing the AST.

class Coder(val ast: Node) {

    var mem = mutableListOf<VMWord>()

    // Addresses to be filled in with a named jump point once coded.
    val forwardJumps = HashMap<String, MutableSet<Int>>()
    // Addresses stored to be used as future jumps.
    val backJumps = HashMap<String, Int>()
    // Stack of lists of 'break' addresses to be used as future jumps.
    val breakJumps = ArrayDeque<MutableSet<Int>>()
    // Stack of lists of 'continue' addresses to be used as future jumps.
    val continueJumps = ArrayDeque<MutableSet<Int>>()
    // Entry points to literal VFuns.
    val blocks = mutableListOf<Executable.Block>()
    // Node currently writing into us.
    lateinit var writer: Node

    fun last() = if (mem.isEmpty()) null else mem[mem.size - 1]

    // Compile the AST into a list of opcodes, by recursively asking the nodes to code themselves.
    // Nodes will then call Coder.code() and Coder.value() to output their compiled code.
    fun generate() {
        ast.code(this)
        if (Yegg.conf.optimizeCompiler) Optimizer(this).optimize()
    }

    // Use this Coder with my Node as the writer.  Returns this Coder, for convenience of: with (coder.use(this))
    fun use(w: Node): Coder {
        writer = w
        return this
    }

    // Code a sub-AST.
    fun code(n: Node) {
        val oldW = writer
        n.code(this)
        writer = oldW
    }

    // Code a sub-AST for assignment context.
    fun codeAssign(n: N_EXPR) {
        val oldW = writer
        n.codeAssign(this)
        writer = oldW
    }

    // Write an opcode into memory.
    fun opcode(op: Opcode) {
        mem.add(VMWord(writer.pos, opcode = op))
    }

    // Write a Value into memory, as an argument to the previous opcode.
    fun value(value: Value) {
        mem.add(VMWord(writer.pos, value = value))
    }
    fun value(intValue: Int) { value(VInt(intValue)) }
    fun value(boolValue: Boolean) { value(VBool(boolValue)) }
    fun value(floatValue: Float) { value(VFloat(floatValue)) }
    fun value(stringValue: String) { value(VString(stringValue)) }
    fun value(objValue: Obj.ID) { value(VObj(objValue)) }
    fun value(listValue: List<Value>) { value(VList(listValue.toMutableList())) }
    fun value(mapValue: Map<Value, Value>) { value(VMap(mapValue.toMutableMap())) }
    fun value(errValue: VMException.Type) { value(VErr(errValue)) }

    // Record a block start address of a literal VFun.
    // Code the block index (as arg for O_FUNVAL).
    // Return the index to be passed later to blockEnd.
    fun codeBlockStart(): Int {
        val entryPoint = blocks.size
        value(entryPoint)
        blocks.add(Executable.Block(mem.size + 2, -1))  // +2 to skip the O_JUMP<addr>
        return entryPoint
    }

    // Record a block end address.  Assumes the start was already coded!
    fun codeBlockEnd(block: Int) {
        blocks[block] = Executable.Block(
            blocks[block].start, mem.size
        )
    }

    // Write a placeholder address for a jump we'll locate in the future.
    // Nodes call this to jump to a named future address.
    fun jumpForward(name: String) {
        val address = mem.size
        val fullname = "$name${writer.id}"
        if (forwardJumps.containsKey(fullname)) {
            forwardJumps[fullname]!!.add(address)
        } else {
            forwardJumps[fullname] = mutableSetOf(address)
        }
        mem.add(VMWord(writer.pos, address = -1))
    }

    // Reach a previously named jump point.  Fill in all previous references with the current address.
    // Nodes call this when a previously named jumpForward address is reached.
    fun setForwardJump(name: String) {
        val dest = mem.size
        val fullname = "$name${writer.id}"
        forwardJumps[fullname]?.forEach { loc ->
            mem[loc].address = dest
        }
        forwardJumps.remove(fullname)
    }

    // Record a jump address we'll jump back to later.
    // Nodes call this to mark a named address which they'll code a jumpBack to.
    fun setBackJump(name: String) {
        val dest = mem.size
        val fullname = "$name${writer.id}"
        backJumps[fullname] = dest
    }

    // Write address of a jump located in the past.
    // Nodes call this to jump to a named past address.
    fun jumpBack(name: String) {
        val fullname = "$name${writer.id}"
        val dest = backJumps[fullname]
        mem.add(VMWord(writer.pos, address = dest))
    }

    // Enter a loop; make lists to store addresses which jump to the break/continue points.
    // After calling this a loop MUST call setBreakJump and setContinueJump at some point!
    fun pushLoopStack() {
        breakJumps.addFirst(mutableSetOf())
        continueJumps.addFirst(mutableSetOf())
    }

    // Write a placeholder address for a break.
    fun jumpBreak() {
        if (breakJumps.isEmpty()) throw CompileException("break outside of loop", writer.pos)
        val address = mem.size
        breakJumps[0].add(address)
        mem.add(VMWord(writer.pos, address = -1))
    }

    // Write the current continue address for a continue jump.
    fun jumpContinue() {
        if (continueJumps.isEmpty()) throw CompileException("continue outside of loop", writer.pos)
        val address = mem.size
        continueJumps[0].add(address)
        mem.add(VMWord(writer.pos, address = -1))
    }

    // Set the current address on all breaks in this loop.
    fun setBreakJump() {
        val dest = mem.size
        breakJumps.removeFirst().forEach { loc ->
            mem[loc].address = dest
        }
    }

    // Set the current address on all continues in this loop.
    fun setContinueJump() {
        val dest = mem.size
        continueJumps.removeFirst().forEach { loc ->
            mem[loc].address = dest
        }
    }

}
