package com.dlfsystems.world.trait

import com.dlfsystems.app.Log
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VM
import com.dlfsystems.vm.VMWord
import com.dlfsystems.vm.dumpText

class Verb(
    val name: String,
) {
    var vm = VM()

    fun program(code: List<VMWord>, variableIDs: Map<String, Int>) {
        vm = VM(listOf<VMWord>() + code, mapOf<String, Int>() + variableIDs)
        Log.d("programmed $name with code ${vm.code.dumpText()}")
    }

    fun call(c: Context, args: List<Value>): Value {
        return vm.execute(c, args)
    }

    fun getListing() = vm.code.dumpText()

}
