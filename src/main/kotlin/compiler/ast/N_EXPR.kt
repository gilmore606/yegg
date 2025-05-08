package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
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

// Parenthetical expressions are parsed to N_PARENS to prevent X.(identifier) from binding as a literal reference.
class N_PARENS(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "($expr)"
    override fun kids() = listOf(expr)
    override fun constantValue() = expr.constantValue()

    override fun code(coder: Coder) {
        if (codeConstant(coder)) return
        expr.code(coder)
    }
}

// Negation of a numeric or boolean.
class N_NEGATE(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "-$expr"
    override fun kids() = listOf(expr)
    override fun constantValue() = expr.constantValue()?.negate()

    override fun code(coder: Coder) {
        if (codeConstant(coder)) return
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
        coder.jumpForward(this, "cond")
        eTrue.code(coder)
        coder.code(this, O_JUMP)
        coder.jumpForward(this, "condFalse")
        coder.setForwardJump(this, "cond")
        eFalse.code(coder)
        coder.setForwardJump(this, "condFalse")
    }
}

// A function call: ident([arg, arg...])
class N_FUNCALL(val name: N_IDENTIFIER, val args: List<N_EXPR>): N_EXPR() {
    override fun kids() = listOf(name) + args

    override fun code(coder: Coder) {
        args.forEach { it.code(coder) }
        coder.code(this, O_FUNCALL)
        coder.value(this, name.name)
        coder.value(this, args.size)
    }
}

// when [expr] { option1 -> expr  option 2 -> { .... expr } else -> ...}
class N_WHEN(val subject: N_EXPR?, val options: List<Pair<N_EXPR?, Node>>, val asStatement: Boolean = false): N_EXPR() {
    override fun kids() = buildList {
        subject?.also { add(it) }
        addAll(options.mapNotNull { it.first })
        addAll(options.map { it.second })
    }

    override fun code(coder: Coder) {
        // non-null match options
        options.filter { it.first != null }.forEachIndexed { n, o ->
            o.first!!.code(coder)
            subject?.also {
                it.code(coder)
                coder.code(this, O_CMP_EQ)
            }
            coder.code(this, O_IF)
            coder.jumpForward(this, "skip$n")
            o.second.code(coder)
            if (asStatement && o.second is N_EXPRSTATEMENT) coder.code(this, O_DISCARD)
            coder.code(this, O_JUMP)
            coder.jumpForward(this, "end")
            coder.setForwardJump(this, "skip$n")
        }
        // else option
        options.firstOrNull { it.first == null }?.also { o ->
            o.second.code(coder)
        }
        coder.setForwardJump(this, "end")
    }
}
