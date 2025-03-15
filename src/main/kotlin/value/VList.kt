package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException.Type.*

class VList(val v: MutableList<Value>): Value() {

    override val type = Type.LIST

    override fun toString() = "[${v.joinToString(", ")}]"

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "length" -> return VInt(v.size)
        }
        return null
    }

    override fun getIndex(c: Context, index: Value): Value? {
        if (index is VInt) {
            if (index.v < 0 || index.v >= v.size) fail(E_RANGE, "list index ${index.v} out of bounds")
            return v[index.v]
        }
        return null
    }

    override fun getRange(c: Context, index1: Value, index2: Value): Value? {
        if (index1 is VInt && index2 is VInt) {
            if (index1.v < 0 || index2.v >= v.size) fail(E_RANGE, "list range ${index1.v}..${index2.v} out of bounds")
            return VList(v.subList(index1.v, index2.v))
        }
        return null
    }

}
