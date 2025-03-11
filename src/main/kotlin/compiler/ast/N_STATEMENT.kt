package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.TokenType
import com.dlfsystems.vm.Opcode.*

abstract class N_STATEMENT: Node() {
    override fun toText(depth: Int): String = tab(depth) + toText()
}

class N_ASSIGN(val left: N_EXPR, val right: N_EXPR, val operator: TokenType = TokenType.T_ASSIGN): N_STATEMENT() {
    override fun toText() = "$left = $right"
    override fun kids() = listOf(left, right)
}

class N_RETURN(val expr: N_EXPR?): N_STATEMENT() {
    override fun toText() = "return $expr"
    override fun kids() = expr?.let { listOf(expr) } ?: listOf()
    override fun code(coder: Coder) {
        expr?.code(coder)
        coder.code(this, O_RETURN)
    }
}

class N_FORLOOP(val assign: N_STATEMENT, val check: N_EXPR, val increment: N_STATEMENT, val body: N_STATEMENT): N_STATEMENT() {
    override fun toText(depth: Int) = tab(depth) + "for ($assign; $check; $increment) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(assign, check, increment, body)
}

class N_WHILELOOP(val check: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    override fun toText(depth: Int) = tab(depth) + "while $check " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(check, body)
}
