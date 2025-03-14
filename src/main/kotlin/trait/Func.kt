package com.dlfsystems.trait

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VM
import com.dlfsystems.vm.VMWord

class Func(
    val name: String,
) {
    var vm = VM()

    fun program(newCode: List<VMWord>) {
        vm = VM(newCode)
    }

    fun execute(context: Context) = vm.execute(context)

    // The function's argument signature.
    class Sig() {

    }
}
