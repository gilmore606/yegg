package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.vm.Opcode.*

class N_FORLOOP(val assign: N_STATEMENT, val check: N_EXPR, val increment: N_STATEMENT, val body: N_STATEMENT): N_STATEMENT() {
    override fun toText(depth: Int) = tab(depth) + "for ($assign; $check; $increment) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(assign, check, increment, body)

    override fun code(c: Coder) {
        c.pushLoopStack()
        assign.code(c)
        c.setBackJump(this, "forStart")
        check.code(c)
        c.opcode(this, O_IF)
        c.jumpForward(this, "forEnd")
        body.code(c)
        c.setContinueJump()
        increment.code(c)
        c.opcode(this, O_JUMP)
        c.jumpBack(this, "forStart")
        c.setForwardJump(this, "forEnd")
        c.setBreakJump()
    }
}

class N_FORVALUE(val index: N_IDENTIFIER, val source: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    private val internalIndex = N_IDENTIFIER(makeID())
    private val internalSource = N_IDENTIFIER(makeID())

    override fun toText(depth: Int) = tab(depth) + "for ($index in $source) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(index, internalIndex, source, internalSource, body)

    override fun code(c: Coder) {
        c.pushLoopStack()
        c.opcode(this, O_VAL)
        c.value(this, 0)
        c.opcode(this, O_SETVAR)
        c.value(this, internalIndex.variableID!!)
        source.code(c)
        c.opcode(this, O_SETGETVAR)
        c.value(this, internalSource.variableID!!)
        c.opcode(this, O_ITERSIZE)
        c.opcode(this, O_CMP_NEZ)
        c.opcode(this, O_IF)
        c.jumpForward(this, "forEnd")
        c.setBackJump(this, "forStart")
        c.opcode(this, O_ITERPICK)
        c.value(this, internalSource.variableID!!)
        c.value(this, internalIndex.variableID!!)
        c.opcode(this, O_SETVAR)
        c.value(this, index.variableID!!)
        body.code(c)
        c.setContinueJump()
        c.opcode(this, O_INCVAR)
        c.value(this, internalIndex.variableID!!)
        c.opcode(this, O_GETVAR)
        c.value(this, internalSource.variableID!!)
        c.opcode(this, O_ITERSIZE)
        c.opcode(this, O_GETVAR)
        c.value(this, internalIndex.variableID!!)
        c.opcode(this, O_CMP_LE)
        c.opcode(this, O_IF)
        c.jumpBack(this, "forStart")
        c.setForwardJump(this, "forEnd")
        c.setBreakJump()
    }
}

class N_FORRANGE(val index: N_IDENTIFIER, val from: N_EXPR, val to: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    private val internalTo = N_IDENTIFIER(makeID())

    override fun toText(depth: Int) = tab(depth) + "for ($index in $from..$to) " + body.toText(depth + 1)
    override fun toText() = toText(0)
    override fun kids() = listOf(index, from, to, internalTo, body)

    override fun code(c: Coder) {
        c.pushLoopStack()
        to.code(c)
        c.opcode(this, O_SETVAR)
        c.value(this, internalTo.variableID!!)
        from.code(c)
        c.opcode(this, O_SETVAR)
        c.value(this, index.variableID!!)
        c.setBackJump(this, "forStart")
        body.code(c)
        c.setContinueJump()
        c.opcode(this, O_INCVAR)
        c.value(this, index.variableID!!)
        c.opcode(this, O_GETVAR)
        c.value(this, internalTo.variableID!!)
        c.opcode(this, O_GETVAR)
        c.value(this, index.variableID!!)
        c.opcode(this, O_CMP_LT)
        c.opcode(this, O_IF)
        c.jumpBack(this, "forStart")
        c.setBreakJump()
    }
}
