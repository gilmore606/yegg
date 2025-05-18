package com.dlfsystems.world.trait

import com.dlfsystems.app.Log
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.vm.VMWord
import com.dlfsystems.vm.dumpText
import com.dlfsystems.vm.Executable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Verb(
    val name: String,
    val traitID: Trait.ID,
): Executable {
    var source = ""
    @Transient override var code: List<VMWord> = listOf()
    @Transient override var symbols: Map<String, Int> = mapOf()
    @Transient override var blocks: List<Executable.Block> = listOf()

    fun program(source: String) {
        this.source = source
        recompile()
        Log.d("programmed $name with code ${code.dumpText()}")
    }

    override fun jitCompile() { if (code.isEmpty()) recompile() }

    private fun recompile() {
        Compiler.compile(source).also {
            code = it.code
            symbols = it.symbols
            blocks = it.blocks
        }
    }

}
