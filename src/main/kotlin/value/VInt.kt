package com.dlfsystems.value

import com.jogamp.opengl.math.FloatUtil.sqrt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.pow

@Serializable
@SerialName("VInt")
data class VInt(val v: Int): Value() {

    @SerialName("yType")
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
    override fun toPower(a2: Value) = when (a2) {
        is VInt -> VInt(v.toDouble().pow(a2.v).toInt())
        is VFloat -> VFloat(v.toFloat().pow(v))
        else -> null
    }
    override fun modulo(a2: Value) = when (a2) {
        is VInt -> VInt(v % a2.v)
        else -> null
    }

    override fun getProp(name: String): Value? {
        when (name) {
            "asFloat" -> return VFloat(v.toFloat())
            "asString" -> return VString(asString())
            "isEven" -> return VBool(v % 2 == 0)
            "isOdd" -> return VBool(v % 2 != 0)
            "sqrt" -> return VFloat(sqrt(v.toFloat()))
        }
        return null
    }

}
