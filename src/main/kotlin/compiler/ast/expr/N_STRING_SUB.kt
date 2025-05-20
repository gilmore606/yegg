package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.literal.N_LITERAL_STRING
import com.dlfsystems.vm.Opcode.O_ADD

// A string with code substitutions.
class N_STRING_SUB(val parts: List<N_EXPR>): N_EXPR() {
    override fun toText() = parts.joinToString { if (it is N_LITERAL_STRING) it.value else "\${$it}" }
    override fun kids() = parts

    override fun code(coder: Coder) {
        when (parts.size) {
            0 -> return
            else -> {
                parts[0].code(coder)
                var i = 1
                while (i < parts.size) {
                    if (!parts[i].isEmptyString()) {
                        parts[i].code(coder)
                        coder.code(this, O_ADD)
                    }
                    i++
                }
            }
        }
    }

    private fun N_EXPR.isEmptyString() = (this is N_LITERAL_STRING) && (this.value == "")
}
