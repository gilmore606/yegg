package com.dlfsystems.compiler

import com.dlfsystems.Yegg
import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VM
import com.dlfsystems.vm.VMException
import com.dlfsystems.vm.VMWord

object Compiler {

    class Result {
        var code: List<VMWord>? = null
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
            result.ast = Shaker(parser.parse()).shake()
            // Stage 4: Generate VM opcodes.
            val coder = Coder(result.ast!!).apply {
                generate()
                postOptimize()
            }
            result.opcodeDump = coder.dumpText()
            result.code = coder.mem
        } catch (e: Exception) {
            result.e = e
        }
        return result
    }



    fun eval(code: String): String {
        val compilerResult = compile(code)
        var vmResult = ""
        compilerResult.code?.also { outcode ->
            try {
                VM(outcode).execute(Context(Yegg.world)).also { vmResult = it.asString() }
            } catch (e: Exception) {
                vmResult = if (e is VMException) e.toString() else e.toString() + "\n" + e.stackTrace.joinToString("\n  ")
            }
        }
        return "TOKENS:\n${compilerResult.tokens}\n\nNODES:\n${compilerResult.ast}\n\nCODE:\n${compilerResult.opcodeDump}\n\nRESULT:\n$vmResult\n"
    }

}
