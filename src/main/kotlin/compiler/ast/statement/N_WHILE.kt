package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_IF
import com.dlfsystems.yegg.vm.Opcode.O_JUMP

class N_WHILE(val check: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    override fun kids() = listOf(check, body)

    override fun code(c: Coder) = with (c.use(this)) {
        pushLoopStack()
        setBackJump("whileStart")
        code(check)
        opcode(O_IF)
        jumpForward("whileEnd")
        code(body)
        setContinueJump()
        opcode(O_JUMP)
        jumpBack("whileStart")
        setForwardJump("whileEnd")
        setBreakJump()
    }
}
