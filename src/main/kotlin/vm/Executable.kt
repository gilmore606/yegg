package com.dlfsystems.yegg.vm

import com.dlfsystems.yegg.value.VFun
import com.dlfsystems.yegg.value.VObj
import com.dlfsystems.yegg.value.Value
import kotlinx.serialization.Serializable

// A block of bytecode which can be executed by a VM.

interface Executable {

    @Serializable
    data class Block(val start: Int, val end: Int)

    val code: List<VMWord>
    val symbols: Map<String, Int>
    val blocks: List<Block>

    // Create a VFun for the given code block and execution parameters.
    fun getLambda(
        block: Int,
        vThis: VObj,
        args: List<String>,
        withVars: Map<String, Value>,
    ): VFun {
        val offset = blocks[block].start
        val lambdaCode = code.subList(blocks[block].start, blocks[block].end).map { word ->
            // Rewrite jump dests with offset
            word.address?.let { VMWord(
                lineNum = word.lineNum,
                charNum = word.charNum,
                address = it - offset
            ) } ?: word
        }
        // Rewrite block borders with offset
        val offsetBlocks = blocks.map { Block(it.start - offset, it.end - offset) }
        return VFun(lambdaCode, symbols, offsetBlocks, vThis, args, withVars)
    }

    // Populate captured scope vars and passed args into initial variable values.
    fun getInitialVars(args: List<Value>): Map<String, Value> = emptyMap()

    // Compile for execution, if needed.
    fun jitCompile() { }

    // Get parent executable for 'pass' expr
    fun getPassExe(): Executable? = null

    // Get source line for stacktrace
    fun getSourceLine(lineNum: Int): String? = null

}
