package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

// An expression which reduces to a Value.

abstract class N_EXPR: N_STATEMENT() {
    // Code this expr as the left side of N_ASSIGN.
    open fun codeAssign(coder: Coder) { fail("illegal left side of assignment") }
}

// Parenthetical expressions are parsed to N_PARENS to prevent X.(identifier) from binding as a literal reference.
class N_PARENS(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "($expr)"
    override fun kids() = listOf(expr)

    override fun code(coder: Coder) {
        expr.code(coder)
    }
}

class N_NEGATE(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "-$expr"
    override fun kids() = listOf(expr)

    override fun code(coder: Coder) {
        expr.code(coder)
        coder.code(this, O_NEGATE)
    }
}

class N_CONDITIONAL(val condition: N_EXPR, val eTrue: N_EXPR, val eFalse: N_EXPR): N_EXPR() {
    override fun toText() = "($condition ? $eTrue : $eFalse)"
    override fun kids() = listOf(condition, eTrue, eFalse)

    override fun code(coder: Coder) {
        condition.code(coder)
        coder.code(this, O_IF)
        coder.jumpForward(this, "cond$id")
        eTrue.code(coder)
        coder.code(this, O_JUMP)
        coder.jumpForward(this, "condFalse$id")
        coder.setForwardJump(this, "cond$id")
        eFalse.code(coder)
        coder.setForwardJump(this, "condFalse$id")
    }
}

class N_INDEX(val left: N_EXPR, val index: N_EXPR): N_EXPR() {
    override fun toText() = "$left[$index]"
    override fun kids() = listOf(left, index)

    override fun code(coder: Coder) {
        // TODO
    }
}
