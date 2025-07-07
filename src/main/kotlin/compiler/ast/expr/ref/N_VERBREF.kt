package com.dlfsystems.yegg.compiler.ast.expr.identifier

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_CALL

class N_VERBREF(val left: N_EXPR, val right: N_EXPR, val args: List<N_EXPR>): N_EXPR() {
    override fun toText() = "$left.$right($args)"
    override fun kids() = mutableListOf(left, right).apply { addAll(args) }

    override fun identify() { (right as? N_IDENTIFIER)?.markAsVerb() }

    override fun code(c: Coder) {
        args.forEach { it.code(c) }
        left.code(c)
        right.code(c)
        c.opcode(this, O_CALL)
        c.value(this, args.size)
    }
}
