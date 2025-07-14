package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.literal.N_LITERAL_STRING
import com.dlfsystems.yegg.vm.Opcode.O_ADD

// A string with code substitutions.
class N_STRING_SUB(val parts: List<N_EXPR>): N_EXPR() {

    override fun toString() = parts.joinToString { if (it is N_LITERAL_STRING) it.value else "\${$it}" }
    override fun kids() = parts

    override fun code(c: Coder) = with (c.use(this)) {
        if (parts.isNotEmpty()) {
            code(parts[0])
            var i = 1
            while (i < parts.size) {
                if (!parts[i].isEmptyString()) {
                    code(parts[i])
                    opcode(O_ADD)
                }
                i++
            }
        }
    }

    private fun N_EXPR.isEmptyString() = (this is N_LITERAL_STRING) && (this.value == "")

}
