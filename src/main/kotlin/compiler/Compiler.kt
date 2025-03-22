package com.dlfsystems.compiler

import com.dlfsystems.Yegg
import com.dlfsystems.app.Log
import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.*

object Compiler {

    class Result {
        var code: List<VMWord>? = null
        var variableIDs: Map<String, Int>? = null
        var tokens: List<Token>? = null
        var ast: Node? = null
        var opcodeDump: String? = null
        var e: Exception? = null
    }

    fun compile(code: String): Result {
        val result = Result()
        try {
            // Stage 1: Lex source into tokens.
            result.tokens = Lexer(code).lex()
            // Stage 2: Parse tokens into AST nodes.
            val parser = Parser(result.tokens!!)
            // Stage 3: Find variables and optimize tree nodes.
            val shaker = Shaker(parser.parse())
            result.ast = shaker.shake()
            result.variableIDs = shaker.variableIDs
            // Stage 4: Generate VM opcodes.
            val coder = Coder(result.ast!!).apply {
                generate()
                postOptimize()
            }
            result.opcodeDump = coder.mem.dumpText()
            result.code = coder.mem
        } catch (e: Exception) {
            result.e = e
        }
        return result
    }



    fun eval(code: String, verbose: Boolean = false): String {
        Log.i("eval: $code")
        val compilerResult = compile(code)
        var vmResult = ""
        compilerResult.code?.also { outcode ->
            Log.i("opcodes: \n${outcode.dumpText()}")
            try {
                VM(outcode).execute(Context(Yegg.world)).also { vmResult = it.asString() }
            } catch (e: Exception) {
                vmResult = if (e is VMException) e.toString() else e.toString() + "\n" + e.stackTrace.joinToString("\n  ")
            }
        }
        return if (verbose)
            "TOKENS:\n${compilerResult.tokens}\n\nNODES:\n${compilerResult.ast}\n\nCODE:\n${compilerResult.opcodeDump}\n\nRESULT:\n$vmResult\n"
        else "$vmResult\n"
    }

}
