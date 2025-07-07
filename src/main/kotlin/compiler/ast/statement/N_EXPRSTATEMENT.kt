package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_DISCARD

class N_EXPRSTATEMENT(val expr: N_EXPR): N_STATEMENT() {
    override fun toText() = expr.toText()
    override fun kids() = listOf(expr)

    override fun code(c: Coder) {
        expr.code(c)
        c.opcode(this, O_DISCARD)
    }
}
