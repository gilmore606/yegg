package com.dlfsystems.world.trait

import com.dlfsystems.app.Log
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.VObj
import com.dlfsystems.value.VTrait
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VM
import com.dlfsystems.vm.VMWord
import com.dlfsystems.vm.dumpText
import kotlinx.serialization.Serializable

@Serializable
class Verb(
    val name: String,
    val traitID: TraitID,
) {
    private var source = ""
    @Transient var code: List<VMWord> = listOf()
    @Transient var symbols: Map<String, Int> = mapOf()
    @Transient var entryPoints: List<Int> = listOf()

    fun program(cOut: Compiler.Result) {
        source = cOut.source
        code = cOut.code
        symbols = cOut.symbols
        entryPoints = cOut.entryPoints
        Log.i("programmed $name with code ${code.dumpText()}")
    }

    fun call(c: Context, vThis: VObj, vTrait: VTrait, args: List<Value>): Value {
        if (source.isNotEmpty() && code.isEmpty()) recompile()
        val vm = VM(this)
        c.push(vThis, vTrait, name, args, vm)
        val r = vm.execute(c, args)
        c.pop()
        return r
    }

    fun getListing() = source

    private fun recompile() {
        Compiler.compile(source).also {
            code = it.code
            symbols = it.symbols
        }
    }

}
