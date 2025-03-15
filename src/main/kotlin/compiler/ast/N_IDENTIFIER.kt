package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

// A bare string in source code, which in context may resolve to a trait, property, or function.
// Without other context, assumed to refer to a variable.

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
                coder.code(this, O_GETVAR)
                coder.value(this, variableID!!)
            }
            else -> {
                coder.code(this, O_VAL)
                coder.value(this, name)
            }
        }
    }

    override fun codeAssign(coder: Coder) {
        if (type == Type.VARIABLE) {
            coder.code(this, O_SETVAR)
            coder.value(this, variableID!!)
        } else fail("non-variable identifier on left of assignment!")
    }

    override fun codeIndexAssign(coder: Coder) {
        if (type == Type.VARIABLE) {
            coder.code(this, O_SETVARI)
            coder.value(this, variableID!!)
        } else fail("non-variable identifier on left of assignment!")
    }
}

class N_PROPREF(val left: N_EXPR, val right: N_EXPR): N_EXPR() {
    override fun toText() = "$left.$right"
    override fun kids() = listOf(left, right)

    override fun identify() { (right as? N_IDENTIFIER)?.markAsProp() }

    override fun code(coder: Coder) {
        left.code(coder)
        right.code(coder)
        coder.code(this, O_GETPROP)
    }

    override fun codeAssign(coder: Coder) {
        left.code(coder)
        right.code(coder)
        coder.code(this, O_SETPROP)
    }
}

class N_FUNCREF(val left: N_EXPR, val args: List<N_EXPR>): N_EXPR() {
    override fun toText() = "$left($args)"
    override fun kids() = mutableListOf(left).apply { addAll(args) }

    override fun identify() { (left as? N_IDENTIFIER)?.markAsFunc() }

    override fun code(coder: Coder) {
        // TODO
    }
}

class N_TRAITREF(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "\$$expr"
    override fun kids() = listOf(expr)

    override fun identify() { (expr as? N_IDENTIFIER)?.markAsTrait() }

    override fun code(coder: Coder) {
        expr.code(coder)
        coder.code(this, O_GETTRAIT)
    }
}
