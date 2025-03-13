package com.dlfsystems.compiler

import com.dlfsystems.Yegg
import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VM
import com.dlfsystems.vm.VMWord
import com.dlfsystems.vm.Value.*

object Compiler {

    sealed class Result {
        class Success(
            val code: List<VMWord>,
            val tokens: List<Token>? = null,
            val ast: Node? = null,
            val dump: String? = null
        ): Result()
        class Failure(val e: Exception): Result()
    }

    fun compile(code: String, withDebug: Boolean = false): Result {
        try {
            // Stage 1: Lex source into tokens.
            val tokens = Lexer(code).lex()
            // Stage 2: Parse tokens into AST nodes.
            val parser = Parser(tokens)
            // Stage 3: Find variables and optimize tree nodes.
            val ast = Shaker(parser.parse()).shake()
            // Stage 4: Generate VM opcodes.
            val coder = Coder(ast).apply { generate() }

            if (withDebug) return Result.Success(coder.mem, tokens, ast, coder.dumpText())
            else return Result.Success(coder.mem)
        } catch (e: Exception) {
            return Result.Failure(e)
        }
    }

    fun eval(code: String): String {
        val result = compile(code, withDebug = true)
        when (result) {
            is Result.Failure -> return "Compilation error: ${result.e}"
            is Result.Success -> {
                try {

                    val context = Context(Yegg.world).apply {
                        vPlayer = VThing(null)
                        vThis = VThing(null)
                    }
                    val returnValue = VM(result.code).execute(context)

                    return "TOKENS:\n${result.tokens}\n\nNODES:\n${result.ast}\n\nCODE:\n${result.dump}\n\nRESULT:\n${returnValue}\n"
                } catch (e: Exception) {
                    return "TOKENS:\n${result.tokens}\n\nNODES:\n${result.ast}\n\nCODE:\n${result.dump}\n\nERROR:\n${e}\n"
                }
            }
        }
    }

}
