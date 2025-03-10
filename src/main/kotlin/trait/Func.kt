package com.dlfsystems.trait

import com.dlfsystems.compiler.Lexer
import com.dlfsystems.compiler.Parser
import com.dlfsystems.compiler.ast.N_BLOCK


class Func(
    val name: String,
    val sig: Sig,
) {
    var source: String? = null
    var ast: N_BLOCK? = null

    fun compile(code: String) {
        ast = Parser(Lexer(code).lex()).parse()
        source = code
    }

    // The function's argument signature.
    class Sig() {

    }
}
