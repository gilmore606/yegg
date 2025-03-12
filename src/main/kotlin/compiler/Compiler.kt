package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.VM

object Compiler {

    fun eval(code: String): String {
        var tokens: List<Token>? = null
        var ast: Node? = null
        var coder: Coder? = null

        try {
            tokens = Lexer(code).lex()
            ast = Shaker(Parser(tokens).parse()).shake()
            coder = Coder(ast)
            coder.generate()
            val result = VM(coder.mem).execute()

            return "tokens:\n\n$tokens\n\nnodes:\n\n$ast\n\ncode:\n\n${coder.dumpText()}\n\nresult: $result\n"
        } catch (e: Exception) {
            return "tokens:\n\n$tokens\n\nnodes:\n\n$ast\n\ncode:\n\n${coder?.dumpText()}\n\nERROR:\n\n$e\n"
        }
    }
}
