package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.statement.N_STATEMENT
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Opcode.*

// An expression which reduces to a Value.

abstract class N_EXPR: N_STATEMENT() {
    // Code this expr as the left side of = assign.
    open fun codeAssign(coder: Coder) { fail("illegal left side of assignment") }
    // Code this expr as the left side of [i]= assign.
    open fun codeIndexAssign(coder: Coder) { fail("illegal left side of index assignment") }
    // Does this expr have a constant value?
    open fun constantValue(): Value? = null
    // If we have a constantValue(), code it and return true.
    // Any code() on an N_EXPR that can have a constant value should call this first (and terminate if true).
    protected fun codeConstant(coder: Coder): Boolean {
        constantValue()?.also { value ->
            coder.code(this, O_VAL)
            coder.value(this, value)
            return true
        }
        return false
    }
}
