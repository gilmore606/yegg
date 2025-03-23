package com.dlfsystems.world.trait

import com.dlfsystems.app.Log
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.VObj
import com.dlfsystems.value.VTrait
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VM
import com.dlfsystems.vm.dumpText

class Verb(
    val name: String,
) {
    var vm = VM()

    fun program(cOut: Compiler.Result) {
        vm = VM(cOut.code, cOut.variableIDs)
        Log.d("programmed $name with code ${vm.code.dumpText()}")
    }

    fun call(c: Context, vThis: VObj, vTrait: VTrait, args: List<Value>): Value {
        c.push(vThis, vTrait, name, args)
        val r = vm.execute(c, args)
        c.pop()
        return r
    }

    fun getListing() = vm.code.dumpText()

}
