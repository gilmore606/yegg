package com.dlfsystems.compiler.ast.expr.ref

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.vm.Opcode.O_GETPROP
import com.dlfsystems.vm.Opcode.O_SETPROP

class N_PROPREF(val left: N_EXPR, val right: N_EXPR): N_EXPR() {
    override fun toText() = "($left.$right)"
    override fun kids() = listOf(left, right)

    override fun identify() { (right as? N_IDENTIFIER)?.markAsProp() }

    override fun code(c: Coder) {
        left.code(c)
        right.code(c)
        c.opcode(this, O_GETPROP)
    }

    override fun codeAssign(c: Coder) {
        left.code(c)
        right.code(c)
        c.opcode(this, O_SETPROP)
    }

}
