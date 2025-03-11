package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.TokenType

abstract class N_STATEMENT: Node() {
    override fun toCode(depth: Int): String = tab(depth) + toCode()
}

class N_ASSIGN(val left: N_EXPR, val right: N_EXPR, val operator: TokenType = TokenType.T_ASSIGN): N_STATEMENT() {
    override fun toCode() = "$left = $right"
    override fun kids() = listOf(left, right)
}

class N_RETURN(val expr: N_EXPR?): N_STATEMENT() {
    override fun toCode() = "return $expr"
    override fun kids() = expr?.let { listOf(expr) } ?: listOf()
}

class N_FORLOOP(val assign: N_STATEMENT, val check: N_EXPR, val increment: N_STATEMENT, val body: N_STATEMENT): N_STATEMENT() {
    override fun toCode(depth: Int) = tab(depth) + "for ($assign; $check; $increment) " + body.toCode(depth + 1)
    override fun toCode() = toCode(0)
    override fun kids() = listOf(assign, check, increment, body)
}

class N_WHILELOOP(val check: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    override fun toCode(depth: Int) = tab(depth) + "while $check " + body.toCode(depth + 1)
    override fun toCode() = toCode(0)
    override fun kids() = listOf(check, body)
}
