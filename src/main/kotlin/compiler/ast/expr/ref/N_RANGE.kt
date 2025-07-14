package com.dlfsystems.yegg.compiler.ast.expr.ref

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_GETRANGE
import com.dlfsystems.yegg.vm.Opcode.O_SETRANGE

// A range index into a value: <expr>[<expr>..<expr>]
class N_RANGE(
    val left: N_EXPR,
    val index1: N_EXPR,
    val index2: N_EXPR
): N_EXPR() {

    override fun toString() = "RANGE<$left[$index1..$index2]>"
    override fun kids() = listOf(left, index1, index2)

    override fun code(c: Coder) = with (c.use(this)) {
        code(left)
        code(index1)
        code(index2)
        opcode(O_GETRANGE)
    }

    override fun codeAssign(c: Coder) = with (c.use(this)) {
        code(left)
        code(index1)
        code(index2)
        opcode(O_SETRANGE)
    }

}
