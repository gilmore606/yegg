package com.dlfsystems.yegg.world.trait

import com.dlfsystems.yegg.compiler.Compiler
import com.dlfsystems.yegg.server.Log
import com.dlfsystems.yegg.vm.VMWord
import com.dlfsystems.yegg.vm.Executable
import com.dlfsystems.yegg.vm.dumpText
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Verb(
    val name: String,
    val traitID: Trait.ID? = null,
): Executable {

    var source = ""
    @Transient override var code: List<VMWord> = listOf()
    @Transient override var symbols: Map<String, Int> = mapOf()
    @Transient override var blocks: List<Executable.Block> = listOf()

    override fun toString() = name

    fun program(source: String) {
        this.source = source.trimIndent()
        recompile()
    }

    override fun jitCompile() { if (code.isEmpty()) recompile() }

    private fun recompile() {
        Compiler.compile(source).also {
            code = it.code
            symbols = it.symbols
            blocks = it.blocks
        }
        Log.d("Compiler", code.dumpText())
    }

    override fun getPassExe() = traitID?.trait()?.getPassVerb(name)

}
