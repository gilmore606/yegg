package com.dlfsystems.yegg.compiler.ast.expr.literal

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.compiler.ast.statement.N_STATEMENT
import com.dlfsystems.yegg.value.VString
import com.dlfsystems.yegg.vm.Opcode.*

class N_LITERAL_FUN(val args: List<N_IDENTIFIER>, val block: N_STATEMENT): N_LITERAL() {
    override fun kids() = args + listOf(block)
    override fun code(c: Coder) = with (c.use(this)) {
        opcode(O_VAL)
        value(args.map { VString(it.name) })
        opcode(O_VAL)
        value(block.collectVars().map { VString(it) })
        opcode(O_FUNVAL)
        val blockID = c.codeBlockStart()
        opcode(O_JUMP)
        jumpForward("skipFun")
        code(block)
        opcode(O_RETURN)
        codeBlockEnd(blockID)
        setForwardJump("skipFun")
    }
}
