package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder

// Parenthetical expressions are parsed to N_PARENS to prevent X.(identifier) from binding as a literal reference.
class N_PARENS(val expr: N_EXPR): N_EXPR() {
    override fun toString() = "($expr)"
    override fun kids() = listOf(expr)
    override fun constantValue() = expr.constantValue()

    override fun code(c: Coder) = with (c.use(this)) {
        if (!codeConstant(c)) {
            code(expr)
        }
    }
}
