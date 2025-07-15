package com.dlfsystems.yegg.value

import com.dlfsystems.yegg.vm.VMException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VErr")
data class VErr(
    val v: VMException.Type,
    val m: String? = null
): Value() {

    override fun equals(other: Any?) = other is VErr && v == other.v
    override fun hashCode() = javaClass.hashCode()

    @SerialName("yType")
    override val type = Type.ERR

    override fun toString() = v.toString()

    override fun cmpEq(a2: Value) = (a2 is VErr && v == a2.v)

}
