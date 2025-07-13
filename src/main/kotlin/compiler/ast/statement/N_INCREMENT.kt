package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.vm.Opcode.O_DECVAR
import com.dlfsystems.yegg.vm.Opcode.O_INCVAR

class N_INCREMENT(val identifier: N_IDENTIFIER, val isDecrement: Boolean = false): N_STATEMENT() {
    override fun toString() = "$identifier++"
    override fun kids() = listOf(identifier)

    override fun code(c: Coder) {
        if (!identifier.isVariable()) fail("cannot increment non-variable identifier")
        with (c.use(this)) {
            if (isDecrement) opcode(O_DECVAR) else opcode(O_INCVAR)
            value(identifier.variableID!!)
        }
    }
}
