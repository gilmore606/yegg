package com.dlfsystems.compiler.ast

class N_BLOCK(val statements: List<N_STATEMENT>): N_STATEMENT() {
    override fun toCode() = toCode(0)
    override fun toCode(depth: Int) = "{\n" + statements.joinToString("") { it.toCode(depth + 1) + "\n" } + tab(depth) + "}"
    override fun kids() = statements
}
