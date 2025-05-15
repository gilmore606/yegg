package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.vm.Opcode.O_IF
import com.dlfsystems.vm.Opcode.O_JUMP


class N_IF(val condition: N_EXPR, val sThen: N_STATEMENT, val sElse: N_STATEMENT? = null): N_STATEMENT() {
    override fun toText() = sElse?.let { "(if $condition $sThen else $sElse)" } ?: "if $condition $sThen"
    override fun kids() = mutableListOf(condition, sThen).apply { sElse?.also { add(it) }}

    override fun code(coder: Coder) {
        condition.code(coder)
        coder.code(this, O_IF)
        coder.jumpForward(this, "ifskip")
        sThen.code(coder)
        sElse?.also { sElse ->
            coder.code(this, O_JUMP)
            coder.jumpForward(this, "elseskip")
            coder.setForwardJump(this, "ifskip")
            sElse.code(coder)
            coder.setForwardJump(this, "elseskip")
        } ?: coder.setForwardJump(this, "ifskip")
    }
}
