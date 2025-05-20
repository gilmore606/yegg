package com.dlfsystems.compiler.ast.expr.identifier

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
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
