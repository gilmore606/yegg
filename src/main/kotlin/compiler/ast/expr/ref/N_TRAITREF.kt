package com.dlfsystems.compiler.ast.expr.identifier

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.vm.Opcode.O_TRAIT

class N_TRAITREF(val expr: N_EXPR): N_EXPR() {
    override fun toText() = "\$$expr"
    override fun kids() = listOf(expr)

    override fun identify() { (expr as? N_IDENTIFIER)?.markAsTrait() }

    override fun code(coder: Coder) {
        expr.code(coder)
        coder.code(this, O_TRAIT)
    }
}
