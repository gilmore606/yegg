package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.vm.Opcode.*

class N_FORLOOP(val assign: N_STATEMENT, val check: N_EXPR, val increment: N_STATEMENT, val body: N_STATEMENT): N_STATEMENT() {
    override fun kids() = listOf(assign, check, increment, body)

    override fun code(c: Coder) = with (c.use(this)) {
        pushLoopStack()
        code(assign)
        setBackJump("forStart")
        code(check)
        opcode(O_IF)
        jumpForward("forEnd")
        code(body)
        setContinueJump()
        code(increment)
        opcode(O_JUMP)
        jumpBack("forStart")
        setForwardJump("forEnd")
        setBreakJump()
    }
}

class N_FORVALUE(val index: N_IDENTIFIER, val source: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    private val internalIndex = N_IDENTIFIER(makeID())
    private val internalSource = N_IDENTIFIER(makeID())

    override fun kids() = listOf(index, internalIndex, source, internalSource, body)

    override fun code(c: Coder) = with (c.use(this)) {
        pushLoopStack()
        opcode(O_VAL)
        value(0)
        opcode(O_SETVAR)
        value(internalIndex.variableID!!)
        code(source)
        opcode(O_SETGETVAR)
        value(internalSource.variableID!!)
        opcode(O_ITERSIZE)
        opcode(O_CMP_NEZ)
        opcode(O_IF)
        jumpForward("forEnd")
        setBackJump("forStart")
        opcode(O_ITERPICK)
        value(internalSource.variableID!!)
        value(internalIndex.variableID!!)
        opcode(O_SETVAR)
        value(index.variableID!!)
        code(body)
        setContinueJump()
        opcode(O_INCVAR)
        value(internalIndex.variableID!!)
        opcode(O_GETVAR)
        value(internalSource.variableID!!)
        opcode(O_ITERSIZE)
        opcode(O_GETVAR)
        value(internalIndex.variableID!!)
        opcode(O_CMP_LE)
        opcode(O_IF)
        jumpBack("forStart")
        setForwardJump("forEnd")
        setBreakJump()
    }
}

class N_FORRANGE(val index: N_IDENTIFIER, val from: N_EXPR, val to: N_EXPR, val body: N_STATEMENT): N_STATEMENT() {
    private val internalTo = N_IDENTIFIER(makeID())

    override fun kids() = listOf(index, from, to, internalTo, body)

    override fun code(c: Coder) = with (c.use(this)) {
        pushLoopStack()
        code(to)
        opcode(O_SETVAR)
        value(internalTo.variableID!!)
        code(from)
        opcode(O_SETVAR)
        value(index.variableID!!)
        setBackJump("forStart")
        code(body)
        setContinueJump()
        opcode(O_INCVAR)
        value(index.variableID!!)
        opcode(O_GETVAR)
        value(internalTo.variableID!!)
        opcode(O_GETVAR)
        value(index.variableID!!)
        opcode(O_CMP_LT)
        opcode(O_IF)
        jumpBack("forStart")
        setBreakJump()
    }
}
