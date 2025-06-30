package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.literal.N_LITERAL_FUN
import com.dlfsystems.vm.Opcode.O_FORK

// fork <expr> { block }
// Returns a VTask with the forked task ID.
class N_FORK(val seconds: N_EXPR, val function: N_LITERAL_FUN): N_EXPR() {
    override fun toText() = "fork ($seconds) $function"
    override fun kids() = listOf(seconds, function)

    override fun code(c: Coder) {
        seconds.code(c)
        function.code(c)
        c.opcode(this, O_FORK)
    }
}
