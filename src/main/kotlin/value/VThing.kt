package com.dlfsystems.value

import com.dlfsystems.vm.Context
import java.util.*

data class VThing(val v: UUID?): Value() {
    override val type = Type.THING

    override fun toString() = "#$v"

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VThing) && (v == a2.v)

    override fun plus(a2: Value) = if (a2 is VString) VString(v.toString() + a2.v) else null

    override fun getProp(context: Context?, propname: String): Value? {
        when (propname) {
            "asString" -> return VString(toString())
        }
        return null
    }

    override fun setProp(context: Context?, propname: String, value: Value): Boolean {
        return false
    }

}
