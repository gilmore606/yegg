package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.world.trait.TraitID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VFun")
data class VFun(
    val name: String,
    val traitID: TraitID,
    val entryPoint: Int,
    val args: List<String>,
): Value() {

    @SerialName("yType")
    override val type = Type.FUN
    override fun toString() = "VFUN"
    override fun asString() = toString()

    override fun callVerb(c: Context, name: String, args: List<Value>): Value? {
        when (name) {
            "invoke" -> return verbInvoke(args)
        }
        return null
    }

    private fun verbInvoke(args: List<Value>): Value {
        return VVoid
    }

}
