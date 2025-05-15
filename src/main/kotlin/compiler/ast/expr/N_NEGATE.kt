package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.O_NEGATE

// Negation of a numeric or boolean.
class N_NEGATE(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "-$expr"
    override fun kids() = listOf(expr)
    override fun constantValue() = expr.constantValue()?.negate()

    override fun code(coder: Coder) {
        if (codeConstant(coder)) return
        expr.code(coder)
        coder.code(this, O_NEGATE)
    }
}
