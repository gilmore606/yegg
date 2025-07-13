package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_IF
import com.dlfsystems.yegg.vm.Opcode.O_JUMP


class N_IF(val condition: N_EXPR, val sThen: N_STATEMENT, val sElse: N_STATEMENT? = null): N_STATEMENT() {
    override fun toString() = sElse?.let { "(if $condition $sThen else $sElse)" } ?: "if $condition $sThen"
    override fun kids() = mutableListOf(condition, sThen).apply { sElse?.also { add(it) }}

    override fun code(c: Coder) = with (c.use(this)) {
        code(condition)
        opcode(O_IF)
        jumpForward("ifskip")
        code(sThen)
        if (sElse != null) {
            opcode(O_JUMP)
            jumpForward("elseskip")
            setForwardJump("ifskip")
            code(sElse)
            setForwardJump("elseskip")
        } else {
            setForwardJump("ifskip")
        }
    }
}
