package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException.Type.E_RANGE
import com.dlfsystems.vm.VMWord
import com.dlfsystems.vm.Executable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VFun")
data class VFun(
    val withCode: List<VMWord>,
    val withSymbols: Map<String, Int>,
    val withBlocks: List<Pair<Int, Int>>,
    val vThis: VObj,
    val argNames: List<String>,
    val vars: Map<String, Value>,
): Value(), Executable {

    @SerialName("yType")
    override val type = Type.FUN
    override fun toString() = "VFUN"
    override fun asString() = toString()

    override val code = buildList { addAll(withCode) }
    override val symbols = buildMap { withSymbols.forEach { put(it.key, it.value) } }
    override val blocks = buildList { addAll(withBlocks) }
    override var jumpOffset = 0

    override fun callVerb(c: Context, name: String, args: List<Value>): Value? {
        when (name) {
            "invoke" -> return verbInvoke(c, args)
        }
        return null
    }

    fun verbInvoke(c: Context, args: List<Value>): Value {
            if ((args.size < argNames.size) || (args.size > argNames.size && args.size > 1))
                fail(E_RANGE, "lambda wants ${argNames.size} args but got ${args.size}")

            return verb.call(c, vThis, listOf(), entryPoint, withVars = buildMap {
                vars.forEach { (name, v) -> put(name, v) }
                argNames.forEachIndexed { n, name ->
                    args.getOrNull(n)?.also { put(name, it) } ?: fail(E_RANGE, "arg $name not found")
                }
                if (argNames.isEmpty() && args.size == 1) put("it", args[0])
            })

    }

}
