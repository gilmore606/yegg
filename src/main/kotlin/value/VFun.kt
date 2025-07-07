package com.dlfsystems.yegg.value

import com.dlfsystems.yegg.util.fail
import com.dlfsystems.yegg.vm.VMException.Type.E_RANGE
import com.dlfsystems.yegg.vm.VMWord
import com.dlfsystems.yegg.vm.Executable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VFun")
data class VFun(
    val withCode: List<VMWord>,
    val withSymbols: Map<String, Int>,
    val withBlocks: List<Executable.Block>,
    val vThis: VObj,
    val argNames: List<String>,
    val vars: Map<String, Value>,
): Value(), Executable {
    override fun equals(other: Any?) = false
    override fun hashCode() = javaClass.hashCode()

    @SerialName("yType")
    override val type = Type.FUN
    override fun toString() = "VFUN"
    override fun asString() = toString()

    override val code = withCode
    override val symbols = withSymbols
    override val blocks = withBlocks

    override fun getInitialVars(args: List<Value>) = buildMap {
        vars.forEach { (name, v) -> put(name, v) }
        if ((args.size < argNames.size) || (args.size > argNames.size && args.size > 1))
            fail(E_RANGE, "lambda wants ${argNames.size} args but got ${args.size}")
        argNames.forEachIndexed { n, name ->
            args.getOrNull(n)?.also { put(name, it) } ?: fail(E_RANGE, "arg $name not found")
        }
        if (argNames.isEmpty() && args.size == 1) put("it", args[0])
    }

}
