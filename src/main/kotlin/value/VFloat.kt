package com.dlfsystems.value

import com.dlfsystems.vm.Context

data class VFloat(val v: Float): Value() {
    override val type = Type.FLOAT

    override fun toString() = v.toString()

    override fun isZero() = v == 0F

    override fun cmpEq(a2: Value) = (a2 is VFloat) && (v == a2.v)
    override fun cmpGt(a2: Value) = (a2 is VFloat) && (v > a2.v)
    override fun cmpGe(a2: Value) = (a2 is VFloat) && (v >= a2.v)

    override fun negate() = VFloat(-v)

    override fun plus(a2: Value) = when (a2) {
        is VInt -> VFloat(v + a2.v.toFloat())
        is VFloat -> VFloat(v + a2.v)
        is VString -> VString(v.toString() + a2.v)
        else -> null
    }
    override fun multiply(a2: Value) = when (a2) {
        is VInt -> VFloat(v * a2.v.toFloat())
        is VFloat -> VFloat(v * a2.v)
        else -> null
    }
    override fun divide(a2: Value) = when (a2) {
        is VInt -> VFloat(v / a2.v.toFloat())
        is VFloat -> VFloat(v / a2.v)
        else -> null
    }

    override fun getProp(context: Context?, propname: String): Value? {
        when (propname) {
            "asInt" -> return VInt(v.toInt())
            "asString" -> return VString(toString())
        }
        return null
    }

}
