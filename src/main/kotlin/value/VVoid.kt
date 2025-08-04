package com.dlfsystems.yegg.value

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VVoid")
data object VVoid: Value() {

    @SerialName("yType")
    override val type = Type.VOID

    override fun toString() = "<VOID>"

}
