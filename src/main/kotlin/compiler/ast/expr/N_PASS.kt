package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.O_PASS

class N_PASS(val args: List<N_EXPR>): N_EXPR() {
    override fun kids() = args

    override fun code(c: Coder) = with (c.use(this)) {
        args.forEach { code(it) }
        opcode(O_PASS)
        value(args.size)
    }
}
