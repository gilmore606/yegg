package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_SUSPEND

class N_SUSPEND(val seconds: N_EXPR): N_STATEMENT() {
    override fun toText() = "suspend ($seconds)"
    override fun kids() = listOf(seconds)

    override fun code(c: Coder) {
        seconds.code(c)
        c.opcode(this, O_SUSPEND)
    }
}
