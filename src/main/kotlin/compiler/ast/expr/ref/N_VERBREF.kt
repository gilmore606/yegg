package com.dlfsystems.compiler.ast.expr.identifier

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.vm.Opcode.O_CALL

class N_VERBREF(val left: N_EXPR, val right: N_EXPR, val args: List<N_EXPR>): N_EXPR() {
    override fun toText() = "$left.$right($args)"
    override fun kids() = mutableListOf(left, right).apply { addAll(args) }

    override fun identify() { (right as? N_IDENTIFIER)?.markAsVerb() }

    override fun code(coder: Coder) {
        args.forEach { it.code(coder) }
        left.code(coder)
        right.code(coder)
        coder.code(this, O_CALL)
        coder.value(this, args.size)
    }
}
