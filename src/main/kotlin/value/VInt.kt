package com.dlfsystems.value

import com.dlfsystems.vm.Context

data class VInt(val v: Int): Value() {

    override val type = Type.INT

    override fun toString() = v.toString()
    override fun asString() = v.toString()

    override fun isZero() = v == 0

    override fun cmpEq(a2: Value) = (a2 is VInt) && (v == a2.v)
    override fun cmpGt(a2: Value) = (a2 is VInt) && (v > a2.v)
    override fun cmpGe(a2: Value) = (a2 is VInt) && (v >= a2.v)

    override fun negate() = VInt(-v)

    override fun plus(a2: Value) = when (a2) {
        is VInt -> VInt(v + a2.v)
        is VFloat -> VFloat(v.toFloat() + a2.v)
        is VString -> VString(asString() + a2.v)
        else -> null
    }
    override fun multiply(a2: Value) = when (a2) {
        is VInt -> VInt(v * a2.v)
        is VFloat -> VFloat(v.toFloat() * a2.v)
        else -> null
    }
    override fun divide(a2: Value) = when (a2) {
        is VInt -> VInt(v / a2.v)
        is VFloat -> VFloat(v.toFloat() / a2.v)
        else -> null
    }

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "asFloat" -> return propAsFloat()
            "asString" -> return propAsString()
        }
        return null
    }


    // Custom props

    private fun propAsFloat() = VFloat(v.toFloat())
    private fun propAsString() = VString(asString())

    // Custom funcs

}
