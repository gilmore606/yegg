package com.dlfsystems.value

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VVoid")
data object VVoid: Value() {

    @SerialName("yType")
    override val type = Type.VOID

    override fun toString() = "<VOID>"
    override fun asString() = ""

    override fun cmpEq(a2: Value): Boolean = a2 is VVoid

    override fun getProp(name: String) = when (name) {
        "asString" -> VString(asString())
        else -> null
    }

}
