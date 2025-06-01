package com.dlfsystems.value

import com.dlfsystems.server.mcp.MCP
import com.dlfsystems.server.mcp.Task
import com.dlfsystems.server.Yegg
import com.dlfsystems.vm.Context
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

    // Valid tasks are true
    override fun isTrue() = MCP.isValidTask(v)

    override fun cmpEq(a2: Value) = (a2 is VTask && v == a2.v)

    override fun getProp(name: String): Value? {
        when (name) {
            "isValid" -> return if (MCP.isValidTask(v)) Yegg.vTrue else Yegg.vFalse
        }
        return null
    }

    override fun callStaticVerb(c: Context, name: String, args: List<Value>): Value? {
        when (name) {
            "cancel" -> return verbCancel(args)
            "resume" -> return verbResume(args)
        }
        return null
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
