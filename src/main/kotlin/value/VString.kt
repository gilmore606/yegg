package com.dlfsystems.value

import com.dlfsystems.vm.Context

data class VString(val v: String): Value() {
    override val type = Type.STRING

    override fun toString() = "\"$v\""

    override fun cmpEq(a2: Value) = (a2 is VString) && (v == a2.v)
    override fun cmpGt(a2: Value) = (a2 is VString) && (v > a2.v)
    override fun cmpGe(a2: Value) = (a2 is VString) && (v >= a2.v)

    override fun plus(a2: Value) = when (a2) {
        is VString -> VString(v + a2.v)
        is VBool -> VString(v + a2.v.toString())
        is VInt -> VString(v + a2.v.toString())
        is VFloat -> VString(v + a2.v.toString())
        else -> null
    }

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "length" -> return VInt(v.length)
            "asInt" -> return VInt(v.toInt())
            "asFloat" -> return VFloat(v.toFloat())
        }
        return null
    }

}
