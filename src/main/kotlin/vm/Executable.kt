package com.dlfsystems.vm

import com.dlfsystems.value.VFun
import com.dlfsystems.value.VObj
import com.dlfsystems.value.Value
import kotlinx.serialization.Serializable

interface Executable {

    @Serializable
    data class Block(val start: Int, val end: Int)

    val code: List<VMWord>
    val symbols: Map<String, Int>
    val blocks: List<Block>

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
        return VFun(lambdaCode, symbols, blocks, vThis, args, withVars)
    }

}
