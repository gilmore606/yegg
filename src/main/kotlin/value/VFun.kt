package com.dlfsystems.value

import com.dlfsystems.server.Yegg
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException.Type.E_RANGE
import com.dlfsystems.vm.VMWord
import com.dlfsystems.vm.Executable
import com.dlfsystems.vm.VM
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

    @SerialName("yType")
    override val type = Type.FUN
    override fun toString() = "VFUN"
    override fun asString() = toString()

    override val code = withCode // no need to copy, it was made on demand
    override val symbols = buildMap { withSymbols.forEach { put(it.key, it.value) } }
    override val blocks = buildList { addAll(withBlocks) }

    override fun captureScope(args: List<Value>) = buildMap {
        vars.forEach { (name, v) -> put(name, v) }
        if ((args.size < argNames.size) || (args.size > argNames.size && args.size > 1))
            fail(E_RANGE, "lambda wants ${argNames.size} args but got ${args.size}")
        argNames.forEachIndexed { n, name ->
            args.getOrNull(n)?.also { put(name, it) } ?: fail(E_RANGE, "arg $name not found")
        }
        if (argNames.isEmpty() && args.size == 1) put("it", args[0])
    }

}
