package com.dlfsystems.yegg.compiler.ast.expr.literal

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.value.VErr
import com.dlfsystems.yegg.vm.Opcode.O_ERRVAL
import com.dlfsystems.yegg.vm.Opcode.O_VAL
import com.dlfsystems.yegg.vm.VMException

class N_LITERAL_ERROR(val value: VMException.Type, val m: N_EXPR? = null): N_LITERAL() {
    override fun toString() = "\"$value\""
    override fun kids() = m?.let { listOf(m) } ?: emptyList()
    override fun constantValue() = if (m == null) VErr(value) else null

    override fun code(c: Coder) = with (c.use(this)) {
        m?.also { code(it) } ?: run {
            opcode(O_VAL)
            value("")
        }
        opcode(O_ERRVAL)
        value(VErr(value))
    }
}
