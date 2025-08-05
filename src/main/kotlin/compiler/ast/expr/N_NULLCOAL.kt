package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.O_IFNON


class N_NULLCOAL(left: N_EXPR, right: N_EXPR): N_BINOP("?:", left, right, listOf()) {
    override fun code(c: Coder) = with (c.use(this)) {
        code(left)
        opcode(O_IFNON)
        jumpForward("nullskip")
        code(right)
        setForwardJump("nullskip")
    }
}
