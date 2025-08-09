package com.dlfsystems.yegg.value

import com.dlfsystems.yegg.server.Yegg
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VNull")
data object VNull: Value() {

    @SerialName("yType")
    override val type = Type.NULL

    override fun toString() = "null"

    override fun isNull() = true
    override fun negate() = Yegg.vTrue
    override fun cmpEq(a2: Value) = a2 == this
    override fun cmpGt(a2: Value) = a2 != this
    override fun cmpGe(a2: Value) = a2 == this

}
