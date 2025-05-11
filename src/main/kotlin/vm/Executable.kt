package com.dlfsystems.vm

import com.dlfsystems.value.VFun
import com.dlfsystems.value.VObj
import com.dlfsystems.value.Value

interface Executable {

    val code: List<VMWord>
    val symbols: Map<String, Int>
    val blocks: List<Pair<Int, Int>>

    fun getLambda(
        block: Int,
        vThis: VObj,
        args: List<String>,
        withVars: Map<String, Value>,
    ): VFun {
        val offset = blocks[block].first
        val code = code.subList(blocks[block].first, blocks[block].second).map { word ->
            // Rewrite jump dests with offset
            word.address?.let { VMWord(
                lineNum = word.lineNum,
                charNum = word.charNum,
                address = it - offset
            ) } ?: word
        }
        return VFun(code, symbols, blocks, vThis, args, withVars)
    }

}
