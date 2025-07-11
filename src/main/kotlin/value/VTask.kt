package com.dlfsystems.yegg.value

import com.dlfsystems.yegg.server.mcp.MCP
import com.dlfsystems.yegg.server.mcp.Task
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.vm.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VTask")
data class VTask(val v: Task.ID): Value() {
    override fun equals(other: Any?) = other is VTask && v == other.v
    override fun hashCode() = javaClass.hashCode()

    @SerialName("yType")
    override val type = Type.TASK

    override fun toString() = "<task:$v>"
    override fun asString() = toString()

    override fun cmpEq(a2: Value) = (a2 is VTask && v == a2.v)

    override fun getProp(name: String) = when (name) {
        "isValid" -> if (MCP.isValidTask(v)) Yegg.vTrue else Yegg.vFalse
        else -> null
    }

    override fun callStaticVerb(c: Context, name: String, args: List<Value>) = when (name) {
        "cancel" -> verbCancel(args)
        "resume" -> verbResume(args)
        else -> null
    }

    private fun verbCancel(args: List<Value>): Value {
        requireArgCount(args, 0, 0)
        MCP.cancel(v)
        return VVoid
    }

    private fun verbResume(args: List<Value>): Value {
        requireArgCount(args, 0, 0)
        MCP.resume(v)
        return VVoid
    }

}
