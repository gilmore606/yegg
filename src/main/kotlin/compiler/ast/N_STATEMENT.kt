package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

// A statement which doesn't necessarily return a value.

abstract class N_STATEMENT: Node() {
    override fun toText(depth: Int): String = tab(depth) + toText()
}

class N_ASSIGN(val left: N_EXPR, val right: N_EXPR): N_STATEMENT() {
    override fun toText() = "$left = $right"
    override fun kids() = listOf(left, right)

    override fun code(coder: Coder) {
        right.code(coder)
        left.codeAssign(coder)
    }
}

class N_RETURN(val expr: N_EXPR?): N_STATEMENT() {
    override fun toText() = "return $expr"
    override fun kids() = expr?.let { listOf(expr) } ?: listOf()

    override fun code(coder: Coder) {
        expr?.code(coder)
        coder.code(this, O_RETURN)
    }
}

class N_FAIL(val expr: N_EXPR): N_STATEMENT() {
    override fun toText() = "fail $expr"
    override fun kids() = listOf(expr)

    override fun code(coder: Coder) {
        expr.code(coder)
        coder.code(this, O_FAIL)
    }
}

class N_FORLOOP(val assign: N_STATEMENT, val check: N_EXPR, val increment: N_STATEMENT, val body: N_STATEMENT): N_STATEMENT() {
    override fun toText(depth: Int) = tab(depth) + "for ($assign; $check; $increment) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(assign, check, increment, body)

    override fun code(coder: Coder) {
        assign.code(coder)
        coder.setBackJump(this, "forStart$id")
        check.code(coder)
        coder.code(this, O_IF)
        coder.jumpForward(this, "forEnd$id")
        body.code(coder)
        increment.code(coder)
        coder.code(this, O_JUMP)
        coder.jumpBack(this, "forStart$id")
        coder.setForwardJump(this, "forEnd$id")
    }
}

class N_WHILELOOP(val check: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    override fun toText(depth: Int) = tab(depth) + "while $check " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(check, body)

    override fun code(coder: Coder) {
        coder.setBackJump(this, "whileStart$id")
        check.code(coder)
        coder.code(this, O_IF)
        coder.jumpForward(this, "whileEnd$id")
        body.code(coder)
        coder.code(this, O_JUMP)
        coder.jumpBack(this, "whileStart$id")
        coder.setForwardJump(this, "whileEnd$id")
    }
}

class N_EXPRSTATEMENT(val expr: N_EXPR): N_STATEMENT() {
    override fun toText() = expr.toText()
    override fun kids() = listOf(expr)

    override fun code(coder: Coder) {
        expr.code(coder)
        // Since this expression is in a statement context, discard its result to avoid stack pollution.
        coder.code(this, O_DISCARD)
    }
}

class N_IFSTATEMENT(val condition: N_EXPR, val sThen: N_STATEMENT, val sElse: N_STATEMENT? = null): N_STATEMENT() {
    override fun toText() = sElse?.let { "(if $condition $sThen else $sElse)" } ?: "if $condition $sThen"
    override fun kids() = mutableListOf(condition, sThen).apply { sElse?.also { add(it) }}

    override fun code(coder: Coder) {
        condition.code(coder)
        coder.code(this, O_IF)
        coder.jumpForward(this, "ifskip$id")
        sThen.code(coder)
        sElse?.also { sElse ->
            coder.code(this, O_JUMP)
            coder.jumpForward(this, "elseskip$id")
            coder.setForwardJump(this, "ifskip$id")
            sElse.code(coder)
            coder.setForwardJump(this, "elseskip$id")
        } ?: coder.setForwardJump(this, "ifskip$id")
    }
}

class N_INCREMENT(val identifier: N_IDENTIFIER, val isDecrement: Boolean = false): N_STATEMENT() {
    override fun toText() = "$identifier++"
    override fun kids() = listOf(identifier)

    override fun code(coder: Coder) {
        if (!identifier.isVariable()) fail("cannot increment non-variable identifier")
        if (isDecrement) coder.code(this, O_DECVAR) else coder.code(this, O_INCVAR)
        coder.value(this, identifier.variableID!!)
    }
}
