package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.O_PASS

class N_PASS(val args: List<N_EXPR>): N_EXPR() {
    override fun kids() = args

    override fun code(coder: Coder) {
        args.forEach { it.code(coder) }
        coder.code(this, O_PASS)
        coder.value(this, args.size)
    }
}
