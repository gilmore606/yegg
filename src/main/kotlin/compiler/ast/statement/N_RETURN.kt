package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_RETURN
import com.dlfsystems.yegg.vm.Opcode.O_RETURNNULL

class N_RETURN(val expr: N_EXPR?): N_STATEMENT() {
    override fun toString() = "return $expr"
    override fun kids() = expr?.let { listOf(expr) } ?: listOf()

    override fun code(c: Coder) = with (c.use(this)) {
        if (expr == null) {
            opcode(O_RETURNNULL)
        } else {
            code(expr)
            opcode(O_RETURN)
        }
    }
}
