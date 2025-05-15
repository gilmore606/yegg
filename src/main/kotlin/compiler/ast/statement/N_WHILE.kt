package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.vm.Opcode.O_IF
import com.dlfsystems.vm.Opcode.O_JUMP

class N_WHILE(val check: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    override fun toText(depth: Int) = tab(depth) + "while $check " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(check, body)

    override fun code(coder: Coder) {
        coder.setBackJump(this, "whileStart")
        check.code(coder)
        coder.code(this, O_IF)
        coder.jumpForward(this, "whileEnd")
        body.code(coder)
        coder.code(this, O_JUMP)
        coder.jumpBack(this, "whileStart")
        coder.setForwardJump(this, "whileEnd")
    }
}
