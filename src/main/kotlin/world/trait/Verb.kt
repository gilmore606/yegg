package com.dlfsystems.world.trait

import com.dlfsystems.compiler.Compiler
import com.dlfsystems.vm.VMWord
import com.dlfsystems.vm.Executable
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
    }

    override fun getPassExe() = traitID?.trait()?.getPassVerb(name)

}
