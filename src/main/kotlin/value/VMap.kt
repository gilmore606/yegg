package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException.Type.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VMap")
data class VMap(val v: MutableMap<Value, Value>): Value() {

    @SerialName("yType")
    override val type = Type.MAP
    override fun toString() = "[${v.entries.joinToString()}]"
    override fun asString() = v.entries.joinToString(", ")

    override fun contains(a2: Value) = v.containsKey(a2)

    override fun plus(a2: Value) = when (a2) {
        is VMap -> VMap((v + a2.v).toMutableMap())
        else -> null
    }

    override fun getProp(name: String): Value? {
        when (name) {
            "size" -> return VInt(v.size)
            "keys" -> return VList(v.keys.toMutableList())
            "values" -> return VList(v.values.toMutableList())
        }
        return null
    }

    override fun getIndex(i: Value): Value? {
        if (v.containsKey(i)) return v[i]
        else fail(E_RANGE, "no map entry $i")
        return null
    }

    override fun setIndex(i: Value, value: Value): Boolean {
        if (i.type !in keyTypes) fail(E_TYPE, "${i.type} cannot be map key")
        v[i] = value
        return true
    }

    override fun callStaticVerb(c: Context, name: String, args: List<Value>): Value? {
        when (name) {
            "hasKey" -> return verbHasKey(args)
            "hasValue" -> return verbHasValue(args)
            "putAll" -> return verbPutAll(args)
            "remove" -> return verbRemove(args)
        }
        return null
    }

    private fun verbHasKey(args: List<Value>): VBool {
        requireArgCount(args, 1, 1)
        return VBool(v.containsKey(args[0]))
    }

    private fun verbHasValue(args: List<Value>): VBool {
        requireArgCount(args, 1, 1)
        return VBool(v.containsValue(args[0]))
    }

    private fun verbPutAll(args: List<Value>): VVoid {
        requireArgCount(args, 1, 1)
        if (args[0].type != Type.MAP) fail(E_TYPE, "${args[0].type} is not map")
        v.putAll((args[0] as VMap).v)
        return VVoid
    }

    private fun verbRemove(args: List<Value>): Value {
        requireArgCount(args, 1, 1)
        if (v.containsKey(args[0])) {
            val removed = v[args[0]]
            v.remove(args[0])
            return removed!!
        }
        return VVoid
    }


    companion object {
        val keyTypes = listOf(Type.STRING, Type.INT, Type.OBJ, Type.TRAIT)

        fun make(v: Map<Value, Value>) = VMap(v.toMutableMap())
    }
}
