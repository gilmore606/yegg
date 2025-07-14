package com.dlfsystems.yegg.compiler.ast.expr.identifier

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.*

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

    override fun variableName() = if (isVariable()) name else null

    override fun code(c: Coder) = with (c.use(this)) {
        when (type) {
            Type.VARIABLE -> {
                opcode(O_GETVAR)
                value(variableID!!)
            }
            else -> {
                opcode(O_VAL)
                value(name)
            }
        }
    }

    override fun codeAssign(c: Coder) = with (c.use(this)) {
        if (type == Type.VARIABLE) {
            opcode(O_SETVAR)
            value(variableID!!)
        } else fail("non-variable identifier on left of assignment!")
    }

}
