package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.value.VString
import com.dlfsystems.vm.Opcode.O_DESTRUCT
import com.dlfsystems.vm.Opcode.O_VAL

class N_DESTRUCT(val vars: List<N_IDENTIFIER>, val right: N_EXPR): N_STATEMENT() {
    override fun toText() = "[${vars.joinToString(",")}] = $right"
    override fun kids() = vars + listOf(right)

    override fun code(coder: Coder) {
        coder.code(this, O_VAL)
        coder.value(this, vars.map { VString(it.name) })
        right.code(coder)
        coder.code(this, O_DESTRUCT)
    }
}
