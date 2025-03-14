package com.dlfsystems.value

import com.dlfsystems.vm.Context

class VMap(val v: MutableMap<Value, Value>): Value() {
    override val type = Type.MAP

    override fun toString() = "[${v.entries.joinToString()}]"

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "length" -> return VInt(v.size)
            "keys" -> return VList(v.keys.toMutableList())
            "values" -> return VList(v.values.toMutableList())
        }
        return null
    }

}
