package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.vm.Opcode.O_DECVAR
import com.dlfsystems.vm.Opcode.O_INCVAR

class N_INCREMENT(val identifier: N_IDENTIFIER, val isDecrement: Boolean = false): N_STATEMENT() {
    override fun toText() = "$identifier++"
    override fun kids() = listOf(identifier)

    override fun code(c: Coder) {
        if (!identifier.isVariable()) fail("cannot increment non-variable identifier")
        if (isDecrement) c.opcode(this, O_DECVAR) else c.opcode(this, O_INCVAR)
        c.value(this, identifier.variableID!!)
    }
}
