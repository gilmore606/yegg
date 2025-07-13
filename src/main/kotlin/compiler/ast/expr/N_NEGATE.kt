package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.O_NEGATE

// Negation of a numeric or boolean.
class N_NEGATE(val expr: N_EXPR): N_EXPR() {
    override fun toString() = "-$expr"
    override fun kids() = listOf(expr)
    override fun constantValue() = expr.constantValue()?.negate()

    override fun code(c: Coder) = with (c.use(this)) {
        if (!codeConstant(c)) {
            code(expr)
            opcode(O_NEGATE)
        }
    }
}
