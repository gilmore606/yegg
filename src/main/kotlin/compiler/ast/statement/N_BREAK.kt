package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.*

class N_BREAK: N_STATEMENT() {
    override fun toText() = "break"

    override fun code(c: Coder) {
        c.opcode(this, O_JUMP)
        c.jumpBreak(this)
    }
}
