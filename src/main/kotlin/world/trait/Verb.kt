package com.dlfsystems.world.trait

import com.dlfsystems.app.Log
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.VObj
import com.dlfsystems.value.VTrait
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VM
import com.dlfsystems.vm.dumpText
import kotlinx.serialization.Serializable

// TODO:
//   Refactor so compiled code is stored here, and
//   vm is instanced in context.callstack with pointer to code.
//   This will be needed when tasks are suspend-able and execution
//   context needs to be preserved.

@Serializable
class Verb(
    val name: String,
) {
    private var vm = VM()
    private var source = ""

    fun program(cOut: Compiler.Result) {
        source = cOut.source
        vm = VM(cOut.code, cOut.symbols)
        Log.d("programmed $name with code ${vm.dumpText()}")
    }

    fun call(c: Context, vThis: VObj, vTrait: VTrait, args: List<Value>): Value {
        c.push(vThis, vTrait, name, args)
        val r = vm.execute(c, args)
        c.pop()
        return r
    }

    fun getListing() = vm.dumpText()

}
