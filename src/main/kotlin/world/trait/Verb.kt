package com.dlfsystems.world.trait

import com.dlfsystems.app.Log
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.server.Yegg
import com.dlfsystems.value.VObj
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VM
import com.dlfsystems.vm.VMWord
import com.dlfsystems.vm.dumpText
import com.dlfsystems.vm.Executable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Verb(
    val name: String,
    val traitID: TraitID,
): Executable {
    var source = ""
    @Transient override var code: List<VMWord> = listOf()
    @Transient override var symbols: Map<String, Int> = mapOf()
    @Transient override var blocks: List<Pair<Int,Int>> = listOf()

    fun program(source: String) {
        this.source = source
        recompile()
        Log.i("programmed $name with code ${code.dumpText()}")
    }

    fun call(
        c: Context, vThis: VObj,
        args: List<Value>,
        withVars: Map<String, Value>? = null
    ): Value {
        if (code.isEmpty()) recompile()
        val vm = VM(this)
        c.push(vThis, Yegg.world.getTrait(traitID)!!.vTrait, name, args, vm)
        val r = vm.execute(c, args, withVars)
        c.pop()
        return r
    }

    private fun recompile() {
        Compiler.compile(source).also {
            code = it.code
            symbols = it.symbols
            blocks = it.blocks
        }
    }

}
