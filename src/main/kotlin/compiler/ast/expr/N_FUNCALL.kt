package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.vm.Opcode.O_FUNCALL

// A function call: ident([arg, arg...])
class N_FUNCALL(val name: N_IDENTIFIER, val args: List<N_EXPR>): N_EXPR() {

    override fun kids() = listOf(name) + args

    override fun code(c: Coder) = with (c.use(this)) {
        args.forEach { code(it) }
        opcode(O_FUNCALL)
        value(name.name)
        value(args.size)
    }

}
