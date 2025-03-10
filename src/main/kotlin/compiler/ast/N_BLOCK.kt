package com.dlfsystems.compiler.ast

class N_BLOCK(val statements: List<N_STATEMENT>): N_STATEMENT() {
    override fun toString() = "{\n" + statements.joinToString("") { "  $it\n" } + "}"
    override fun kids() = statements
}
