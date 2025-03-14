package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException.Type.*

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

    override fun getIndex(c: Context, index: Value): Value? {
        if (index is VInt) {
            if (index.v < 0 || index.v >= v.length) fail(E_RANGE, "string index ${index.v} out of bounds")
            return VString(v[index.v].toString())
        }
        return null
    }

    override fun getRange(c: Context, index1: Value, index2: Value): Value? {
        if (index1 is VInt && index2 is VInt) {
            if (index1.v < 0 || index2.v >= v.length) fail(E_RANGE, "string range ${index1.v}..${index2.v} out of bounds")
            return VString(v.substring(index1.v, index2.v))
        }
        return null
    }

}
