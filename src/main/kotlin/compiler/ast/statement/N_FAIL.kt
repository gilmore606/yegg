package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.vm.Opcode.O_FAIL

class N_FAIL(val expr: N_EXPR): N_STATEMENT() {
    override fun toText() = "fail $expr"
    override fun kids() = listOf(expr)

    override fun code(c: Coder) {
        expr.code(c)
        c.opcode(this, O_FAIL)
    }
}
