package com.dlfsystems.yegg.value

import com.dlfsystems.yegg.util.fail
import com.dlfsystems.yegg.vm.Context
import com.dlfsystems.yegg.vm.VMException.Type.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VMap")
data class VMap(val v: MutableMap<Value, Value>): Value() {
    override fun equals(other: Any?) = other is VMap && v == other.v
    override fun hashCode() = javaClass.hashCode()

    @SerialName("yType")
    override val type = Type.MAP
    override fun toString() = "[${v.entries.joinToString()}]"
    override fun asString() = v.entries.joinToString(", ")

    override fun contains(a2: Value) = v.containsKey(a2)

    override fun plus(a2: Value) = when (a2) {
        is VMap -> VMap((v + a2.v).toMutableMap())
        else -> null
    }

    override fun getProp(name: String) = when (name) {
        "size" -> VInt(v.size)
        "keys" -> VList.make(v.keys.toList())
        "values" -> VList.make(v.values.toList())
        else -> null
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

    override fun callStaticVerb(c: Context, name: String, args: List<Value>) = when (name) {
        "hasKey" -> verbHasKey(args)
        "hasValue" -> verbHasValue(args)
        "putAll" -> verbPutAll(args)
        "remove" -> verbRemove(args)
        else -> null
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
        return VNull
    }


    companion object {
        val keyTypes = listOf(Type.STRING, Type.INT, Type.OBJ, Type.TRAIT)

        fun make(v: Map<Value, Value>) = VMap(v.toMutableMap())
    }
}
