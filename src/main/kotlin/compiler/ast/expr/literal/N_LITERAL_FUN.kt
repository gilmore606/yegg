package com.dlfsystems.compiler.ast.expr.literal

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.compiler.ast.statement.N_STATEMENT
import com.dlfsystems.value.VString
import com.dlfsystems.vm.Opcode.*

class N_LITERAL_FUN(val args: List<N_IDENTIFIER>, val block: N_STATEMENT): N_LITERAL() {
    override fun kids() = args + listOf(block)
    override fun code(c: Coder) {
        c.opcode(this, O_VAL)
        c.value(this, args.map { VString(it.name) })
        c.opcode(this, O_VAL)
        c.value(this, block.collectVars().map { VString(it) })
        c.opcode(this, O_FUNVAL)
        val blockID = c.codeBlockStart(this)
        c.opcode(this, O_JUMP)
        c.jumpForward(this, "skipFun")
        block.code(c)
        c.opcode(this, O_RETURN)
        c.codeBlockEnd(this, blockID)
        c.setForwardJump(this, "skipFun")
    }
}
