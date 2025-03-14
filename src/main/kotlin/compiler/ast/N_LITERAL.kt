package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

abstract class N_LITERAL: N_EXPR()

class N_LITERAL_BOOLEAN(val value: Boolean): N_LITERAL() {
    override fun toText() = if (value) "true" else "false"
    override fun code(coder: Coder) {
        coder.code(this, O_LITERAL)
        coder.value(this, value)
    }
}

class N_LITERAL_INTEGER(val value: Int): N_LITERAL() {
    override fun toText() = "$value"
    override fun code(coder: Coder) {
        coder.code(this, O_LITERAL)
        coder.value(this, value)
    }
}

class N_LITERAL_FLOAT(val value: Float): N_LITERAL() {
    override fun toText() = "$value"
    override fun code(coder: Coder) {
        coder.code(this, O_LITERAL)
        coder.value(this, value)
    }
}

class N_LITERAL_STRING(val value: String): N_LITERAL() {
    override fun toText() = "\"$value\""
    override fun code(coder: Coder) {
        coder.code(this, O_LITERAL)
        coder.value(this, value)
    }
}
