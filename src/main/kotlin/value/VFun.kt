package com.dlfsystems.value

import com.dlfsystems.server.Yegg
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException
import com.dlfsystems.vm.VMException.Type.E_RANGE
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
    val argNames: List<String>,
    val vars: Map<String, Value>,
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
            if ((args.size < argNames.size) || (args.size > argNames.size && args.size > 1))
                fail(E_RANGE, "lambda wants ${argNames.size} args but got ${args.size}")

            return verb.call(c, vThis, listOf(), entryPoint, withVars = buildMap {
                vars.forEach { (name, v) -> put(name, v) }
                argNames.forEachIndexed { n, name ->
                    args.getOrNull(n)?.also { put(name, it) } ?: fail(E_RANGE, "arg $name not found")
                }
                if (argNames.isEmpty() && args.size == 1) put("it", args[0])
            })

        } ?: fail(VMException.Type.E_SYS, "missing verb for VFun ($traitID.$name)")
        return VVoid
    }

}
