package com.dlfsystems.compiler

import com.dlfsystems.Yegg
import com.dlfsystems.app.Log
import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.*

object Compiler {

    class Result(
        val code: List<VMWord>,
        val variableIDs: Map<String, Int>,
        val tokens: List<Token>,
        val ast: Node,
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
            // Stage 3: Find variables.
            val shaker = Shaker(parser.parse())
            ast = shaker.shake()
            variableIDs = shaker.variableIDs
            // Stage 4: Generate VM opcodes.
            code = Coder(ast).generate()
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
        Log.d("eval: $code")
        var cOut: Compiler.Result? = null
        try {
            cOut = compile(code)
            Log.d("  opcodes: \n${cOut.code.dumpText()}")
            val vmOut = VM(cOut.code).execute(Context(Yegg.world)).asString()
            return if (verbose) dumpText(cOut.tokens, cOut.ast, cOut.code, vmOut) else vmOut
        } catch (e: Exception) {
            return if (verbose) {
                if (e is CompileException) dumpText(e.tokens, e.ast, e.code, "")
                else dumpText(cOut?.tokens, cOut?.ast, cOut?.code, "")
            } else e.toString()
        }
    }

    private fun dumpText(tokens: List<Token>?, ast: Node?, code: List<VMWord>?, result: String?): String =
        "TOKENS:\n${tokens}\n\nNODES:\n${ast}\n\nCODE:\n${code?.dumpText()}\nRESULT:\n$result\n"

}
