package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

class N_BREAK: N_STATEMENT() {
    override fun toText() = "break"

    override fun code(coder: Coder) {
        coder.code(this, O_JUMP)
        coder.jumpBreak(this)
    }
}
