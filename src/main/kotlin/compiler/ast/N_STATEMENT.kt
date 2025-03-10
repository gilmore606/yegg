package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.TokenType

abstract class N_STATEMENT: Node()

class N_ASSIGN(val ident: String, val operator: TokenType, val right: N_EXPR): N_STATEMENT() {
    override fun toString() = "$ident $operator $right"
    override fun kids() = listOf(right)
}

class N_RETURN(val expr: N_EXPR): N_STATEMENT() {
    override fun toString() = "return $expr"
    override fun kids() = listOf(expr)
}
