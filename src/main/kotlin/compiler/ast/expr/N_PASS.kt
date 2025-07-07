package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.O_PASS

class N_PASS(val args: List<N_EXPR>): N_EXPR() {
    override fun kids() = args

    override fun code(c: Coder) {
        args.forEach { it.code(c) }
        c.opcode(this, O_PASS)
        c.value(this, args.size)
    }
}
