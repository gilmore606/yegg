package com.dlfsystems.trait

import com.dlfsystems.compiler.Lexer
import com.dlfsystems.compiler.Parser
import com.dlfsystems.compiler.ast.N_BLOCK
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VM
import com.dlfsystems.vm.VMWord


class Func(
    val name: String,
) {
    var code: List<VMWord>? = null
    var vm: VM? = null

    fun program(newCode: List<VMWord>) {
        code = newCode
        vm = VM(newCode)
    }

    fun execute(context: Context) = vm?.execute(context)

    // The function's argument signature.
    class Sig() {

    }
}
