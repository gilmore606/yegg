package com.dlfsystems.compiler.ast.expr.literal

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.value.VList
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Opcode.O_LISTVAL

class N_LITERAL_LIST(val value: List<N_EXPR>): N_LITERAL() {
    override fun kids() = value
    override fun toText() = value.joinToString(", ", "LIST[", "]")
    override fun constantValue(): Value? {
        val constant = mutableListOf<Value>()
        for (expr in value) {
            val exprConstant = expr.constantValue()
            if (exprConstant == null) return null else constant.add(exprConstant)
        }
        return VList(constant)
    }
    override fun code(c: Coder) {
        if (codeConstant(c)) return
        value.forEach { it.code(c) }
        c.opcode(this, O_LISTVAL)
        c.value(this, value.size)
    }
}
