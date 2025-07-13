package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.Coder

class N_BLOCK(val statements: List<N_STATEMENT>): N_STATEMENT() {
    override fun kids() = statements

    override fun code(c: Coder) = with (c.use(this)) {
        statements.forEach { code(it) }
    }
}
