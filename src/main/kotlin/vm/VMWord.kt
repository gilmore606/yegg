package com.dlfsystems.vm

import com.dlfsystems.value.VInt
import com.dlfsystems.value.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// An atom of VM opcode memory.
// Can hold an Opcode, a Value, or an int representing a memory address (for jumps).
@Serializable
data class VMWord(
    @SerialName("l") val lineNum: Int,
    @SerialName("c") val charNum: Int,
    @SerialName("o") val opcode: Opcode? = null,
    @SerialName("v") val value: Value? = null,
    @SerialName("a") var address: Int? = null,
) {
    // In compilation, an address word may be written before the address it points to is known.
    // fillAddress is called to set it once calculated.
    fun fillAddress(newAddress: Int) { address = newAddress }

    override fun toString() = opcode?.toString() ?: value?.toString() ?: address?.let { "<$it>" } ?: "!!NULL!!"

    fun isInt(equals: Int? = null): Boolean {
        if (value !is VInt) return false
        if (equals == null) return true
        return (value.v == equals)
    }

    // If this is known to be an int opcode arg, just get the int value.
    val intFromV: Int
        get() = (value as VInt).v
}

fun List<VMWord>.dumpText(): String {
    if (isEmpty()) return "<not programmed>\n"
    var s = ""
    var pc = 0
    while (pc < size) {
        val cell = get(pc)
        s += "<$pc> "
        s += cell.toString()
        cell.opcode?.also { opcode ->
            repeat (opcode.argCount) {
                pc++
                val arg = if (pc < size) get(pc).toString() else "??"
                s += " $arg"
            }
        }
        s += "\n"
        pc++
    }
    return s
}
