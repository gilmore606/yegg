package com.dlfsystems.value

import com.dlfsystems.vm.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VVoid(val v: Unit = Unit): Value() {

    @SerialName("yType")
    override val type = Type.VOID

    override fun toString() = "<VOID>"
    override fun asString() = ""

    override fun cmpEq(a2: Value): Boolean = a2 is VVoid

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "asString" -> return propAsString()
        }
        return null
    }


    // Custom props

    private fun propAsString() = VString(asString())

    // Custom verbs

}
