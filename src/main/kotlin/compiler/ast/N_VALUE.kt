package com.dlfsystems.compiler.ast

abstract class N_VALUE: N_EXPR()

class N_IDENTIFIER(val name: String): N_VALUE() {
    override fun toCode() = "$name"
}

class N_GENERIC(val expr: N_EXPR): N_VALUE() {
    override fun toCode() = "\$$expr"
    override fun kids() = listOf(expr)
}

abstract class N_LITERAL: N_VALUE()

class N_LITERAL_BOOLEAN(val value: Boolean): N_LITERAL() {
    override fun toCode() = if (value) "true" else "false"
}

class N_LITERAL_INTEGER(val value: Int): N_LITERAL() {
    override fun toCode() = "$value"
}

class N_LITERAL_FLOAT(val value: Float): N_LITERAL() {
    override fun toCode() = "$value"
}

class N_LITERAL_STRING(val value: String): N_LITERAL() {
    override fun toCode() = "\"$value\""
}
