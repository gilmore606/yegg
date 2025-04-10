package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

// An expression which reduces to a Value.

abstract class N_EXPR: N_STATEMENT() {
    // Code this expr as the left side of = assign.
    open fun codeAssign(coder: Coder) { fail("illegal left side of assignment") }
    // Code this expr as the left side of [i]= assign.
    open fun codeIndexAssign(coder: Coder) { fail("illegal left side of index assignment") }
}

// Parenthetical expressions are parsed to N_PARENS to prevent X.(identifier) from binding as a literal reference.
class N_PARENS(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "($expr)"
    override fun kids() = listOf(expr)

    override fun code(coder: Coder) {
        expr.code(coder)
    }
}

// Negation of a numeric or boolean.
class N_NEGATE(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "-$expr"
    override fun kids() = listOf(expr)

    override fun code(coder: Coder) {
        expr.code(coder)
        coder.code(this, O_NEGATE)
    }
}

// A string with code substitutions.
class N_STRING_SUB(val parts: List<N_EXPR>): N_EXPR() {
    override fun toText() = parts.joinToString { if (it is N_LITERAL_STRING) it.value else "\${$it}" }
    override fun kids() = parts

    override fun code(coder: Coder) {
        when (parts.size) {
            0 -> N_LITERAL_STRING("").code(coder)
            else -> {
                parts[0].code(coder)
                var i = 1
                while (i < parts.size) {
                    if (!(parts[i] is N_LITERAL_STRING && (parts[i] as N_LITERAL_STRING).value == "")) {
                        parts[i].code(coder)
                        coder.code(this, O_ADD)
                    }
                    i++
                }
            }
        }
    }
}

// A three-part conditional expression: (cond) ? trueExpr : falseExpr
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
