package com.dlfsystems.vm

interface Executable {

    val code: List<VMWord>
    val symbols: Map<String, Int>
    val blocks: List<Pair<Int, Int>>
    var jumpOffset: Int

    fun getBlockCode(block: Int): List<VMWord> =
        code.subList(blocks[block].first, blocks[block].second)  // SHIT!
    // TODO: this needs to remap addresses!
    // TODO: start is offset, subtract from all address args
    // alternate plan: just pass the offset  to VM and have it apply it to all addresses/jumps?!?!

}
