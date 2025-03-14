package com.dlfsystems.value

import com.dlfsystems.vm.Context

class VList(val v: MutableList<Value>): Value() {
    override val type = Type.LIST

    override fun toString() = "[${v.joinToString(", ")}]"

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "length" -> return VInt(v.size)
        }
        return null
    }
}
