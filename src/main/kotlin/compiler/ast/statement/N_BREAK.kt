package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.vm.Opcode.*

class N_BREAK: N_STATEMENT() {
    override fun toString() = "break"

    override fun code(c: Coder) = with (c.use(this)) {
        opcode(O_JUMP)
        jumpBreak()
    }
}
