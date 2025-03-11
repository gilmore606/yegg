package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node

object Compiler {

    fun eval(code: String): String {
        var tokens: List<Token>? = null
        var ast: Node? = null
        try {
            tokens = Lexer(code).lex()
            ast = Shaker(Parser(tokens).parse()).shake()
            return "tokens:\n\n$tokens\n\nnodes:\n\n$ast\n"
        } catch (e: CompileException) {
            return "tokens:\n\n$tokens\n\nERROR:\n\n$e\n"
        }
    }
}
