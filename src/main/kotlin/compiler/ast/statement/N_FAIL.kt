package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_FAIL

class N_FAIL(val expr: N_EXPR): N_STATEMENT() {
    override fun toText() = "fail $expr"
    override fun kids() = listOf(expr)

    override fun code(c: Coder) {
        expr.code(c)
        c.opcode(this, O_FAIL)
    }
}
