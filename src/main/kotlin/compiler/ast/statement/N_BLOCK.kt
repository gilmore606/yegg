package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder

class N_BLOCK(val statements: List<N_STATEMENT>): N_STATEMENT() {
    override fun toText() = toText(0)
    override fun toText(depth: Int) = "{\n" + statements.joinToString("") { it.toText(depth + 1) + "\n" } + tab(depth) + "}"
    override fun kids() = statements

    override fun code(c: Coder) {
        statements.forEach { it.code(c) }
    }
}
