package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.statement.N_STATEMENT
import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.vm.Opcode.*

// An expression which reduces to a Value.

abstract class N_EXPR: N_STATEMENT() {
    // Code this expr as the left side of = assign.
    open fun codeAssign(c: Coder) { fail("illegal left side of assignment") }
    // Code this expr as the left side of [i]= assign.
    // TODO: is this used?  do we need to implement this or something?
    open fun codeIndexAssign(c: Coder) { fail("illegal left side of index assignment") }
    // Does this expr have a constant value?
    open fun constantValue(): Value? = null
    // If we have a constantValue(), code it and return true.
    // Any code() on an N_EXPR that can have a constant value should call this first (and terminate if true).
    protected fun codeConstant(c: Coder): Boolean {
        constantValue()?.also { value ->
            c.opcode(this, O_VAL)
            c.value(this, value)
            return true
        }
        return false
    }
}
