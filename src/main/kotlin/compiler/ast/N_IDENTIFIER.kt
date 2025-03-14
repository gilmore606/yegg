package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*


class N_IDENTIFIER(val name: String): N_EXPR() {
    enum class Type { VARIABLE, TRAIT_NAME, PROP_NAME, FUNC_NAME }
    var type: Type = Type.VARIABLE

    fun markAsTrait() { type = Type.TRAIT_NAME }
    fun markAsProp() { type = Type.PROP_NAME }
    fun markAsFunc() { type = Type.FUNC_NAME }

    fun isVariable() = type == Type.VARIABLE
    var variableID: Int? = null

    override fun toText() = "$name"

    override fun code(coder: Coder) {
        when (type) {
            Type.VARIABLE -> {
                coder.code(this, O_FETCHVAR)
                coder.value(this, variableID!!)
            }
            else -> {
                coder.code(this, O_LITERAL)
                coder.value(this, name)
            }
        }
    }

    override fun codeAssign(coder: Coder) {
        coder.code(this, O_STOREVAR)
        coder.value(this, variableID!!)
    }
}

class N_PROPREF(val left: N_EXPR, val right: N_EXPR): N_EXPR() {
    override fun toText() = "$left.$right"
    override fun kids() = listOf(left, right)
    override fun identify() {
        (right as? N_IDENTIFIER)?.markAsProp()
    }
    override fun code(coder: Coder) {
        left.code(coder)
        right.code(coder)
        coder.code(this, O_FETCHPROP)
    }
}

class N_FUNCREF(val left: N_EXPR, val args: List<N_EXPR>): N_EXPR() {
    override fun toText() = "$left($args)"
    override fun kids() = mutableListOf(left).apply { addAll(args) }
    override fun identify() {
        (left as? N_IDENTIFIER)?.markAsFunc()
    }
}

class N_TRAITREF(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "\$$expr"
    override fun kids() = listOf(expr)
    override fun identify() {
        (expr as? N_IDENTIFIER)?.markAsTrait()
    }
    override fun code(coder: Coder) {
        expr.code(coder)
        coder.code(this, O_FETCHTRAIT)
    }
}
