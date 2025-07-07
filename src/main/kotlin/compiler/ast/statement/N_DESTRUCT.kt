package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.value.VString
import com.dlfsystems.yegg.vm.Opcode.O_DESTRUCT
import com.dlfsystems.yegg.vm.Opcode.O_VAL

class N_DESTRUCT(val vars: List<N_IDENTIFIER>, val right: N_EXPR): N_STATEMENT() {
    override fun toText() = "[${vars.joinToString(",")}] = $right"
    override fun kids() = vars + listOf(right)

    override fun code(c: Coder) {
        c.opcode(this, O_VAL)
        c.value(this, vars.map { VString(it.name) })
        right.code(c)
        c.opcode(this, O_DESTRUCT)
    }
}
