package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.vm.Opcode.O_RETURN
import com.dlfsystems.vm.Opcode.O_RETURNNULL

class N_RETURN(val expr: N_EXPR?): N_STATEMENT() {
    override fun toText() = "return $expr"
    override fun kids() = expr?.let { listOf(expr) } ?: listOf()

    override fun code(c: Coder) {
        expr?.code(c)
        c.opcode(this, if (expr == null) O_RETURNNULL else O_RETURN)
    }
}
