package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR

class N_ASSIGN(val left: N_EXPR, val right: N_EXPR): N_STATEMENT() {
    override fun toText() = "$left = $right"
    override fun kids() = listOf(left, right)

    override fun code(coder: Coder) {
        right.code(coder)
        left.codeAssign(coder)
    }
}
