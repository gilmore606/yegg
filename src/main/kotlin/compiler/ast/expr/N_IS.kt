package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.O_ISTRAIT
import com.dlfsystems.yegg.vm.Opcode.O_ISTYPE


class N_ISTRAIT(left: N_EXPR, right: N_EXPR): N_BINOP("is", left, right, listOf(O_ISTRAIT))

class N_ISTYPE(val left: N_EXPR, val typeID: Int): N_EXPR() {
    override fun kids() = listOf(left)

    override fun code(c: Coder) = with (c.use(this)) {
        code(left)
        opcode(O_ISTYPE)
        value(typeID)
    }
}
