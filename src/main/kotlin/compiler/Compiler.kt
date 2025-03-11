package com.dlfsystems.compiler

object Compiler {

    fun eval(code: String): String {
        var tokens: List<Token>? = null
        try {
            tokens = Lexer(code).lex()
            val ast = Parser(tokens).parse()
            return "tokens:\n\n$tokens\n\nnodes:\n\n$ast\n"
        } catch (e: CompileException) {
            return "tokens:\n\n$tokens\n\nERROR:\n\n$e\n"
        }
    }
}
