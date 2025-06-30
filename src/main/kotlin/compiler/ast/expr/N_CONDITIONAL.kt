package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.O_IF
import com.dlfsystems.vm.Opcode.O_JUMP

// A three-part conditional expression: (cond) ? trueExpr : falseExpr
class N_CONDITIONAL(val condition: N_EXPR, val eTrue: N_EXPR, val eFalse: N_EXPR): N_EXPR() {
    override fun toText() = "($condition ? $eTrue : $eFalse)"
    override fun kids() = listOf(condition, eTrue, eFalse)

    override fun code(c: Coder) {
        condition.code(c)
        c.opcode(this, O_IF)
        c.jumpForward(this, "cond")
        eTrue.code(c)
        c.opcode(this, O_JUMP)
        c.jumpForward(this, "condFalse")
        c.setForwardJump(this, "cond")
        eFalse.code(c)
        c.setForwardJump(this, "condFalse")
    }
}
