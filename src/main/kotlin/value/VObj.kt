package com.dlfsystems.value

import com.dlfsystems.vm.Context
import java.util.*

data class VObj(val v: UUID?): Value() {

    override val type = Type.OBJ

    override fun toString() = "#$v"
    override fun asString() = "OBJ" // TODO: use name from passed context?
    override fun asMapKey() = "$v OBJ"

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VObj) && (v == a2.v)

    override fun plus(a2: Value) = if (a2 is VString) VString(v.toString() + a2.v) else null

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "asString" -> return propAsString()
        }
        return null
    }

    override fun setProp(c: Context, name: String, value: Value): Boolean {
        return false
    }


    // Custom props

    private fun propAsString() = VString(toString())

    // Custom funcs

}
