package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.Value
import com.dlfsystems.vm.Value.Type.*

abstract class N_VALUE: N_EXPR()

class N_IDENTIFIER(val name: String): N_VALUE() {
    override fun toText() = "$name"
}

class N_GENERIC(val expr: N_EXPR): N_VALUE() {
    override fun toText() = "\$$expr"
    override fun kids() = listOf(expr)
}

abstract class N_LITERAL: N_VALUE()

class N_LITERAL_BOOLEAN(val value: Boolean): N_LITERAL() {
    override fun toText() = if (value) "true" else "false"
    override fun code(coder: Coder) {
        coder.code(this, Opcode.O_LITERAL)
        coder.value(this, Value(BOOL, boolV = value))
    }
}

class N_LITERAL_INTEGER(val value: Int): N_LITERAL() {
    override fun toText() = "$value"
    override fun code(coder: Coder) {
        coder.code(this, Opcode.O_LITERAL)
        coder.value(this, Value(INT, intV = value))
    }
}

class N_LITERAL_FLOAT(val value: Float): N_LITERAL() {
    override fun toText() = "$value"
    override fun code(coder: Coder) {
        coder.code(this, Opcode.O_LITERAL)
        coder.value(this, Value(FLOAT, floatV = value))
    }
}

class N_LITERAL_STRING(val value: String): N_LITERAL() {
    override fun toText() = "\"$value\""
    override fun code(coder: Coder) {
        coder.code(this, Opcode.O_LITERAL)
        coder.value(this, Value(STRING, stringV = value))
    }
}
