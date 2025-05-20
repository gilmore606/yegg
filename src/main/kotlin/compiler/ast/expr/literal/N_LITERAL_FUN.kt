package com.dlfsystems.compiler.ast.expr.literal

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.compiler.ast.statement.N_STATEMENT
import com.dlfsystems.value.VString
import com.dlfsystems.vm.Opcode.*

class N_LITERAL_FUN(val args: List<N_IDENTIFIER>, val block: N_STATEMENT): N_LITERAL() {
    override fun kids() = args + listOf(block)
    override fun code(coder: Coder) {
        coder.code(this, O_VAL)
        coder.value(this, args.map { VString(it.name) })
        coder.code(this, O_VAL)
        coder.value(this, block.collectVars().map { VString(it) })
        coder.code(this, O_FUNVAL)
        val blockID = coder.codeBlockStart(this)
        coder.code(this, O_JUMP)
        coder.jumpForward(this, "skipFun")
        block.code(coder)
        coder.code(this, O_RETURN)
        coder.codeBlockEnd(this, blockID)
        coder.setForwardJump(this, "skipFun")
    }
}
