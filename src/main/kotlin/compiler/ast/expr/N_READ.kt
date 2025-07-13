package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.O_READLINE
import com.dlfsystems.yegg.vm.Opcode.O_READLINES

class N_READ(val singleLine: Boolean, val args: List<N_EXPR>): N_EXPR() {
    override fun kids() = args

    override fun code(c: Coder) = with (c.use(this)) {
        opcode(if (singleLine) O_READLINE else O_READLINES)
    }
}
