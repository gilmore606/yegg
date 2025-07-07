package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.O_NEGATE

// Negation of a numeric or boolean.
class N_NEGATE(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "-$expr"
    override fun kids() = listOf(expr)
    override fun constantValue() = expr.constantValue()?.negate()

    override fun code(c: Coder) {
        if (codeConstant(c)) return
        expr.code(c)
        c.opcode(this, O_NEGATE)
    }
}
