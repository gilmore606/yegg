package com.dlfsystems.value

import com.dlfsystems.server.Yegg
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException
import com.dlfsystems.world.trait.TraitID
import com.dlfsystems.world.trait.Verb
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VFun")
data class VFun(
    val name: String,
    val traitID: TraitID,
    val vThis: VObj,
    val entryPoint: Int,
    val args: List<String>,
): Value() {

    @SerialName("yType")
    override val type = Type.FUN
    override fun toString() = "\$${Yegg.world.getTrait(traitID)?.name ?: "INVALID"}.$name#$entryPoint()"
    override fun asString() = toString()

    private fun getVerb(): Verb? = Yegg.world.getTrait(traitID)?.verbs?.get(name)

    override fun callVerb(c: Context, name: String, args: List<Value>): Value? {
        when (name) {
            "invoke" -> return verbInvoke(c, args)
        }
        return null
    }

    private fun verbInvoke(c: Context, args: List<Value>): Value {
        getVerb()?.also { verb ->
            // TODO: pass args somehow
            return verb.call(c, vThis, listOf(), entryPoint)
        } ?: fail(VMException.Type.E_SYS, "missing verb for VFun ($traitID.$name)")
        return VVoid
    }

}
