package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR

class N_ASSIGN(val left: N_EXPR, val right: N_EXPR): N_STATEMENT() {
    override fun toText() = "$left = $right"
    override fun kids() = listOf(left, right)

    override fun code(c: Coder) {
        right.code(c)
        left.codeAssign(c)
    }
}
