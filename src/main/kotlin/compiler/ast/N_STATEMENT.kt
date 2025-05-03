package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.value.VString
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

class N_SPREAD(val vars: List<N_IDENTIFIER>, val right: N_EXPR): N_STATEMENT() {
    override fun toText() = "[${vars.joinToString(",")}] = $right"
    override fun kids() = vars + listOf(right)

    override fun code(coder: Coder) {
        coder.code(this, O_VAL)
        coder.value(this, vars.map { VString(it.name) })
        right.code(coder)
        coder.code(this, O_SPREAD)
    }
}

class N_RETURN(val expr: N_EXPR?): N_STATEMENT() {
    override fun toText() = "return $expr"
    override fun kids() = expr?.let { listOf(expr) } ?: listOf()

    override fun code(coder: Coder) {
        expr?.code(coder)
        coder.code(this, if (expr == null) O_RETURNNULL else O_RETURN)
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

class N_FORVALUE(val index: N_IDENTIFIER, val source: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    private val internalIndex = N_IDENTIFIER(makeID().toString())
    private val internalSource = N_IDENTIFIER(makeID().toString())

    override fun toText(depth: Int) = tab(depth) + "for ($index in $source) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(index, internalIndex, source, internalSource, body)

    override fun code(coder: Coder) {
        coder.code(this, O_VAL)
        coder.value(this, 0)
        coder.code(this, O_SETVAR)
        coder.value(this, internalIndex.variableID!!)
        source.code(coder)
        coder.code(this, O_SETVAR)
        coder.value(this, internalSource.variableID!!)
        coder.setBackJump(this, "forStart$id")
        coder.code(this, O_ITERPICK)
        coder.value(this, internalSource.variableID!!)
        coder.value(this, internalIndex.variableID!!)
        coder.code(this, O_SETVAR)
        coder.value(this, index.variableID!!)
        body.code(coder)
        coder.code(this, O_INCVAR)
        coder.value(this, internalIndex.variableID!!)
        coder.code(this, O_GETVAR)
        coder.value(this, internalSource.variableID!!)
        coder.code(this, O_ITERSIZE)
        coder.code(this, O_GETVAR)
        coder.value(this, internalIndex.variableID!!)
        coder.code(this, O_CMP_LE)
        coder.code(this, O_IF)
        coder.jumpBack(this, "forStart$id")
    }
}

class N_FORRANGE(val index: N_IDENTIFIER, val from: N_EXPR, val to: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    private val internalTo = N_IDENTIFIER(makeID().toString())

    override fun toText(depth: Int) = tab(depth) + "for ($index in $from..$to) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(index, from, to, internalTo, body)

    override fun code(coder: Coder) {
        to.code(coder)
        coder.code(this, O_SETVAR)
        coder.value(this, internalTo.variableID!!)
        from.code(coder)
        coder.code(this, O_SETVAR)
        coder.value(this, index.variableID!!)
        coder.setBackJump(this, "forStart$id")
        body.code(coder)
        coder.code(this, O_INCVAR)
        coder.value(this, index.variableID!!)
        coder.code(this, O_GETVAR)
        coder.value(this, internalTo.variableID!!)
        coder.code(this, O_GETVAR)
        coder.value(this, index.variableID!!)
        coder.code(this, O_CMP_LT)
        coder.code(this, O_IF)
        coder.jumpBack(this, "forStart$id")
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
        // TODO: Determine if there are actual circumstances this would matter.  I can't think of any --
        // TODO: expressions can't have statements inside them, so stack integrity seems to only matter
        // TODO: within an expression no matter how deep.  If we leave a value on the stack for every
        // TODO: naked statement it only matters for heap use during execution.  I don't really care.
        // TODO: Not doing this lets us pick the last exprstatement value off the stack for eval.
        // coder.code(this, O_DISCARD)
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
