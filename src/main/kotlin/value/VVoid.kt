package com.dlfsystems.value

import com.dlfsystems.vm.Context

data class VVoid(val v: Unit = Unit): Value() {
    override val type = Type.VOID

    override fun toString() = "<VOID>"

    override fun cmpEq(a2: Value): Boolean = a2 is VVoid

    override fun getProp(c: Context, propname: String): Value? {
        when (propname) {
            "asString" -> return VString(toString())
        }
        return null
    }

}
