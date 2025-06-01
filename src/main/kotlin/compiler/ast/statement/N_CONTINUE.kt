package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

class N_CONTINUE: N_STATEMENT() {
    override fun toText() = "continue"

    override fun code(coder: Coder) {
        coder.code(this, O_JUMP)
        coder.jumpContinue(this)
    }
}
