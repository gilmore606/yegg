package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.O_IF
import com.dlfsystems.yegg.vm.Opcode.O_JUMP

// A three-part conditional expression: (cond) ? trueExpr : falseExpr
class N_CONDITIONAL(
    val condition: N_EXPR,
    val eTrue: N_EXPR,
    val eFalse: N_EXPR
): N_EXPR() {

    override fun toString() = "($condition ? $eTrue : $eFalse)"
    override fun kids() = listOf(condition, eTrue, eFalse)

    override fun code(c: Coder) = with (c.use(this)) {
        code(condition)
        opcode(O_IF)
        jumpForward("cond")
        code(eTrue)
        opcode(O_JUMP)
        jumpForward("condFalse")
        setForwardJump("cond")
        code(eFalse)
        setForwardJump("condFalse")
    }

}
