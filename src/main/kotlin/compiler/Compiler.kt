package com.dlfsystems.compiler

import com.dlfsystems.Yegg
import com.dlfsystems.app.Log
import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.*

object Compiler {

    class Result(
        var code: List<VMWord>,
        var variableIDs: Map<String, Int>,
        var tokens: List<Token>,
        var ast: Node,
    )

    fun compile(source: String): Result {
        var tokens: List<Token>? = null
        var code: List<VMWord>? = null
        var ast: Node? = null
        var variableIDs: Map<String, Int>? = null
        try {
            // Stage 1: Lex source into tokens.
            tokens = Lexer(source).lex()
            // Stage 2: Parse tokens into AST nodes.
            val parser = Parser(tokens)
            // Stage 3: Find variables and optimize tree nodes.
            val shaker = Shaker(parser.parse())
            ast = shaker.shake()
            variableIDs = shaker.variableIDs
            // Stage 4: Generate VM opcodes.
            val coder = Coder(ast).apply {
                generate()
                postOptimize()
            }
            code = coder.mem
            return Result(code, variableIDs, tokens, ast)
        } catch (e: Exception) {
            throw (if (e is CompileException) e else CompileException("COMPILER CRASH: " + e.stackTraceToString(), 0, 0)).apply {
                code = code
                variableIDs = variableIDs
                tokens = tokens
                ast = ast
            }
        }
    }

    fun eval(code: String, verbose: Boolean = false): String {
        Log.i("eval: $code")
        var cOut: Compiler.Result? = null
        try {
            cOut = compile(code)
            Log.i("opcodes: \n${cOut.code.dumpText()}")
            val vmOut = VM(cOut.code).execute(Context(Yegg.world)).asString()
            return if (verbose) {
                "TOKENS:\n${cOut.tokens}\n\nNODES:\n${cOut.ast}\n\nCODE:\n${cOut.code.dumpText()}\nRESULT:\n$vmOut\n"
            } else "$vmOut\n"
        } catch (e: Exception) {
            return if (verbose) {
                if (e is CompileException) "TOKENS:\n${e.tokens}\n\nNODES:\n${e.ast}\n\nCODE:\n${e.code?.dumpText()}\n"
                else "TOKENS:\n${cOut?.tokens}\n\nNODES:\n${cOut?.ast}\n\nCODE:\n${cOut?.code?.dumpText()}\n"
            } else e.toString()
        }
    }

}
