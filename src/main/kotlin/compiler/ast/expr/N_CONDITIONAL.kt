package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.O_IF
import com.dlfsystems.vm.Opcode.O_JUMP

// A three-part conditional expression: (cond) ? trueExpr : falseExpr
class N_CONDITIONAL(val condition: N_EXPR, val eTrue: N_EXPR, val eFalse: N_EXPR): N_EXPR() {
    override fun toText() = "($condition ? $eTrue : $eFalse)"
    override fun kids() = listOf(condition, eTrue, eFalse)

    override fun code(coder: Coder) {
        condition.code(coder)
        coder.code(this, O_IF)
        coder.jumpForward(this, "cond")
        eTrue.code(coder)
        coder.code(this, O_JUMP)
        coder.jumpForward(this, "condFalse")
        coder.setForwardJump(this, "cond")
        eFalse.code(coder)
        coder.setForwardJump(this, "condFalse")
    }
}
