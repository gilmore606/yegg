package com.dlfsystems.yegg.compiler.ast.expr.identifier

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_TRAIT

class N_TRAITREF(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "\$$expr"
    override fun kids() = listOf(expr)

    override fun identify() { (expr as? N_IDENTIFIER)?.markAsTrait() }

    override fun code(c: Coder) {
        expr.code(c)
        c.opcode(this, O_TRAIT)
    }
}
