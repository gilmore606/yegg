package com.dlfsystems.compiler.ast.expr.ref

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.vm.Opcode.O_GETI
import com.dlfsystems.vm.Opcode.O_SETI

// An index into a value: <expr>[<expr>]
class N_INDEX(val left: N_EXPR, val index: N_EXPR): N_EXPR() {
    override fun toText() = "INDEX<$left[$index]>"
    override fun kids() = listOf(left, index)

    override fun code(c: Coder) {
        left.code(c)
        index.code(c)
        c.opcode(this, O_GETI)
    }

    override fun codeAssign(c: Coder) {
        left.code(c)
        index.code(c)
        c.opcode(this, O_SETI)
    }
}
