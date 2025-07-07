package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.vm.Opcode.O_DECVAR
import com.dlfsystems.yegg.vm.Opcode.O_INCVAR

class N_INCREMENT(val identifier: N_IDENTIFIER, val isDecrement: Boolean = false): N_STATEMENT() {
    override fun toText() = "$identifier++"
    override fun kids() = listOf(identifier)

    override fun code(c: Coder) {
        if (!identifier.isVariable()) fail("cannot increment non-variable identifier")
        if (isDecrement) c.opcode(this, O_DECVAR) else c.opcode(this, O_INCVAR)
        c.value(this, identifier.variableID!!)
    }
}
