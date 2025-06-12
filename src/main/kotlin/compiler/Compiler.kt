package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.compiler.parser.Lexer
import com.dlfsystems.compiler.parser.Token
import com.dlfsystems.compiler.parser.Parser
import com.dlfsystems.vm.*

// Compile a source string into executable code.

object Compiler {

    class Result(
        val source: String,
        val code: List<VMWord>,
        val symbols: Map<String, Int>,
        val ast: Node,
        val blocks: List<Executable.Block>,
    )

    fun compile(source: String): Result {
        var tokens: List<Token>? = null
        var code: List<VMWord>? = null
        var ast: Node? = null
        var symbols: Map<String, Int>? = null
        var blocks: List<Executable.Block>? = null
        try {
            // Stage 1: Lex source into tokens.
            tokens = Lexer(source).lex()
            // Stage 2: Parse tokens into AST nodes.
            val parser = Parser(tokens)
            // Stage 3: Find variables.
            val shaker = Shaker(parser.parse())
            ast = shaker.shake()
            symbols = shaker.symbols
            // Stage 4: Generate VM opcodes.
            val coder = Coder(ast).apply { generate() }
            code = coder.mem
            blocks = coder.blocks
            return Result(source, code, symbols, ast, blocks)
        } catch (e: CompileException) {
            throw e.withInfo(code, symbols, tokens, ast)
        } catch (e: Exception) {
            throw CompileException("GURU: " + e.stackTraceToString(), 0, 0)
                .withInfo(code, symbols, tokens, ast)
        }
    }

}
