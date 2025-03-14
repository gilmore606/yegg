package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException.Type.*

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

    override fun getIndex(c: Context, index: Value): Value? {
        if (v.containsKey(index)) return v[index]
        else fail(E_RANGE, "no map entry $index")
        return null
    }
}
