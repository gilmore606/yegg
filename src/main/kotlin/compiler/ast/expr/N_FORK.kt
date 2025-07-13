package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.literal.N_LITERAL_FUN
import com.dlfsystems.yegg.vm.Opcode.O_FORK

// fork <expr> { block }
// Returns a VTask with the forked task ID.
class N_FORK(val seconds: N_EXPR, val function: N_LITERAL_FUN): N_EXPR() {
    override fun toString() = "fork ($seconds) $function"
    override fun kids() = listOf(seconds, function)

    override fun code(c: Coder) = with (c.use(this)) {
        code(seconds)
        code(function)
        opcode(O_FORK)
    }
}
