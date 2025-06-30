package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.vm.Opcode.O_IF
import com.dlfsystems.vm.Opcode.O_JUMP

class N_WHILE(val check: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    override fun toText(depth: Int) = tab(depth) + "while $check " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(check, body)

    override fun code(c: Coder) {
        c.pushLoopStack()
        c.setBackJump(this, "whileStart")
        check.code(c)
        c.opcode(this, O_IF)
        c.jumpForward(this, "whileEnd")
        body.code(c)
        c.setContinueJump()
        c.opcode(this, O_JUMP)
        c.jumpBack(this, "whileStart")
        c.setForwardJump(this, "whileEnd")
        c.setBreakJump()
    }
}
