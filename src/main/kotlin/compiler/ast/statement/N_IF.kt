package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_IF
import com.dlfsystems.yegg.vm.Opcode.O_JUMP


class N_IF(val condition: N_EXPR, val sThen: N_STATEMENT, val sElse: N_STATEMENT? = null): N_STATEMENT() {
    override fun toText() = sElse?.let { "(if $condition $sThen else $sElse)" } ?: "if $condition $sThen"
    override fun kids() = mutableListOf(condition, sThen).apply { sElse?.also { add(it) }}

    override fun code(c: Coder) {
        condition.code(c)
        c.opcode(this, O_IF)
        c.jumpForward(this, "ifskip")
        sThen.code(c)
        sElse?.also { sElse ->
            c.opcode(this, O_JUMP)
            c.jumpForward(this, "elseskip")
            c.setForwardJump(this, "ifskip")
            sElse.code(c)
            c.setForwardJump(this, "elseskip")
        } ?: c.setForwardJump(this, "ifskip")
    }
}
