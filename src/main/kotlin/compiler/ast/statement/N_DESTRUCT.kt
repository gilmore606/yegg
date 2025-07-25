package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.value.VInt
import com.dlfsystems.yegg.value.VString
import com.dlfsystems.yegg.vm.Opcode.O_DESTRUCT
import com.dlfsystems.yegg.vm.Opcode.O_VAL

class N_DESTRUCT(
    val vars: List<N_IDENTIFIER>,
    val types: List<Int>,
    val right: N_EXPR
): N_STATEMENT() {
    override fun toString() = "[${vars.joinToString(",")}] = $right"
    override fun kids() = vars + listOf(right)

    override fun code(c: Coder) = with (c.use(this)) {
        opcode(O_VAL)
        value(vars.map { VString(it.name) })
        opcode(O_VAL)
        value(types.map { VInt(it) })
        code(right)
        opcode(O_DESTRUCT)
    }
}
