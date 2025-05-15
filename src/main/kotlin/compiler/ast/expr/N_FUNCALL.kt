package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.vm.Opcode.O_FUNCALL

// A function call: ident([arg, arg...])
class N_FUNCALL(val name: N_IDENTIFIER, val args: List<N_EXPR>): N_EXPR() {
    override fun kids() = listOf(name) + args

    override fun code(coder: Coder) {
        args.forEach { it.code(coder) }
        coder.code(this, O_FUNCALL)
        coder.value(this, name.name)
        coder.value(this, args.size)
    }
}
