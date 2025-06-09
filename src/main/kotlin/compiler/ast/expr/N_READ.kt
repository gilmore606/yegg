package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.O_READLINE
import com.dlfsystems.vm.Opcode.O_READLINES

class N_READ(val singleLine: Boolean, val args: List<N_EXPR>): N_EXPR() {
    override fun kids() = args

    override fun code(coder: Coder) {
        coder.code(this, if (singleLine) O_READLINE else O_READLINES)
    }
}
