package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.*

class N_CONTINUE: N_STATEMENT() {
    override fun toText() = "continue"

    override fun code(c: Coder) {
        c.opcode(this, O_JUMP)
        c.jumpContinue(this)
    }
}
