package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

// A bare string in source code, which in context may resolve to a trait, property, or verb.
// Without other context, assumed to refer to a variable.

class N_IDENTIFIER(val name: String): N_EXPR() {
    enum class Type { VARIABLE, TRAIT_NAME, PROP_NAME, VERB_NAME }
    var type: Type = Type.VARIABLE

    fun markAsTrait() { type = Type.TRAIT_NAME }
    fun markAsProp() { type = Type.PROP_NAME }
    fun markAsVerb() { type = Type.VERB_NAME }

    fun isVariable() = type == Type.VARIABLE
    var variableID: Int? = null

    override fun toText() = "$name"

    override fun variableName() = if (isVariable()) name else null

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
}

// An index into a value: <expr>[<expr>]
class N_INDEX(val left: N_EXPR, val index: N_EXPR): N_EXPR() {
    override fun toText() = "INDEX<$left[$index]>"
    override fun kids() = listOf(left, index)

    override fun code(coder: Coder) {
        left.code(coder)
        index.code(coder)
        coder.code(this, O_GETI)
    }

    override fun codeAssign(coder: Coder) {
        left.code(coder)
        index.code(coder)
        coder.code(this, O_SETI)
    }
}

// A range index into a value: <expr>[<expr>..<expr>]
class N_RANGE(val left: N_EXPR, val index1: N_EXPR, val index2: N_EXPR): N_EXPR() {
    override fun toText() = "RANGE<$left[$index1..$index2]>"
    override fun kids() = listOf(left, index1, index2)

    override fun code(coder: Coder) {
        left.code(coder)
        index1.code(coder)
        index2.code(coder)
        coder.code(this, O_GETRANGE)
    }

    override fun codeAssign(coder: Coder) {
        left.code(coder)
        index1.code(coder)
        index2.code(coder)
        coder.code(this, O_SETRANGE)
    }
}

class N_PROPREF(val left: N_EXPR, val right: N_EXPR): N_EXPR() {
    override fun toText() = "($left.$right)"
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

class N_VERBREF(val left: N_EXPR, val right: N_EXPR, val args: List<N_EXPR>): N_EXPR() {
    override fun toText() = "$left.$right($args)"
    override fun kids() = mutableListOf(left, right).apply { addAll(args) }

    override fun identify() { (right as? N_IDENTIFIER)?.markAsVerb() }

    override fun code(coder: Coder) {
        args.forEach { it.code(coder) }
        left.code(coder)
        right.code(coder)
        coder.code(this, O_CALL)
        coder.value(this, args.size)
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
