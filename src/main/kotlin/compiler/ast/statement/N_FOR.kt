package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.vm.Opcode.*

class N_FORLOOP(val assign: N_STATEMENT, val check: N_EXPR, val increment: N_STATEMENT, val body: N_STATEMENT): N_STATEMENT() {
    override fun toText(depth: Int) = tab(depth) + "for ($assign; $check; $increment) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(assign, check, increment, body)

    override fun code(coder: Coder) {
        coder.pushLoopStack()
        assign.code(coder)
        coder.setBackJump(this, "forStart")
        check.code(coder)
        coder.code(this, O_IF)
        coder.jumpForward(this, "forEnd")
        body.code(coder)
        coder.setContinueJump()
        increment.code(coder)
        coder.code(this, O_JUMP)
        coder.jumpBack(this, "forStart")
        coder.setForwardJump(this, "forEnd")
        coder.setBreakJump()
    }
}

class N_FORVALUE(val index: N_IDENTIFIER, val source: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    private val internalIndex = N_IDENTIFIER(makeID())
    private val internalSource = N_IDENTIFIER(makeID())

    override fun toText(depth: Int) = tab(depth) + "for ($index in $source) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(index, internalIndex, source, internalSource, body)

    override fun code(coder: Coder) {
        coder.pushLoopStack()
        coder.code(this, O_VAL)
        coder.value(this, 0)
        coder.code(this, O_SETVAR)
        coder.value(this, internalIndex.variableID!!)
        source.code(coder)
        coder.code(this, O_SETVAR)
        coder.value(this, internalSource.variableID!!)
        coder.setBackJump(this, "forStart")
        coder.code(this, O_ITERPICK)
        coder.value(this, internalSource.variableID!!)
        coder.value(this, internalIndex.variableID!!)
        coder.code(this, O_SETVAR)
        coder.value(this, index.variableID!!)
        body.code(coder)
        coder.setContinueJump()
        coder.code(this, O_INCVAR)
        coder.value(this, internalIndex.variableID!!)
        coder.code(this, O_GETVAR)
        coder.value(this, internalSource.variableID!!)
        coder.code(this, O_ITERSIZE)
        coder.code(this, O_GETVAR)
        coder.value(this, internalIndex.variableID!!)
        coder.code(this, O_CMP_LE)
        coder.code(this, O_IF)
        coder.jumpBack(this, "forStart")
        coder.setBreakJump()
    }
}

class N_FORRANGE(val index: N_IDENTIFIER, val from: N_EXPR, val to: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    private val internalTo = N_IDENTIFIER(makeID())

    override fun toText(depth: Int) = tab(depth) + "for ($index in $from..$to) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(index, from, to, internalTo, body)

    override fun code(coder: Coder) {
        coder.pushLoopStack()
        to.code(coder)
        coder.code(this, O_SETVAR)
        coder.value(this, internalTo.variableID!!)
        from.code(coder)
        coder.code(this, O_SETVAR)
        coder.value(this, index.variableID!!)
        coder.setBackJump(this, "forStart")
        body.code(coder)
        coder.setContinueJump()
        coder.code(this, O_INCVAR)
        coder.value(this, index.variableID!!)
        coder.code(this, O_GETVAR)
        coder.value(this, internalTo.variableID!!)
        coder.code(this, O_GETVAR)
        coder.value(this, index.variableID!!)
        coder.code(this, O_CMP_LT)
        coder.code(this, O_IF)
        coder.jumpBack(this, "forStart")
        coder.setBreakJump()
    }
}
