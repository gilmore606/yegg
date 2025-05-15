package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.vm.Opcode.O_SUSPEND

class N_SUSPEND(val seconds: N_EXPR): N_STATEMENT() {
    override fun toText() = "suspend ($seconds)"
    override fun kids() = listOf(seconds)

    override fun code(coder: Coder) {
        seconds.code(coder)
        coder.code(this, O_SUSPEND)
    }
}
