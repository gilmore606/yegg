package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

class N_BREAK: N_STATEMENT() {
    override fun toText() = "break"

    override fun code(c: Coder) {
        c.opcode(this, O_JUMP)
        c.jumpBreak(this)
    }
}
