package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder

// Parenthetical expressions are parsed to N_PARENS to prevent X.(identifier) from binding as a literal reference.
class N_PARENS(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "($expr)"
    override fun kids() = listOf(expr)
    override fun constantValue() = expr.constantValue()

    override fun code(coder: Coder) {
        if (codeConstant(coder)) return
        expr.code(coder)
    }
}
