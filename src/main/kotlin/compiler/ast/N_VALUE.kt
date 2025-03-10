package com.dlfsystems.compiler.ast

abstract class N_VALUE: N_EXPR()

class N_IDENTIFIER(val name: String): N_VALUE() {
    override fun toString() = "$name"
}

abstract class N_LITERAL: N_VALUE()

class N_LITERAL_BOOLEAN(val value: Boolean): N_LITERAL() {
    override fun toString() = if (value) "true" else "false"
}

class N_LITERAL_INTEGER(val value: Int): N_LITERAL() {
    override fun toString() = "INT<$value>"
}

class N_LITERAL_FLOAT(val value: Float): N_LITERAL() {
    override fun toString() = "FLOAT<$value>"
}

class N_LITERAL_STRING(val value: String): N_LITERAL() {
    override fun toString() = "STRING<\"$value\">"
}
