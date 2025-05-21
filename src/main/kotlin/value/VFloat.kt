package com.dlfsystems.value

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
@SerialName("VFloat")
data class VFloat(val v: Float): Value() {

    @SerialName("yType")
    override val type = Type.FLOAT

    override fun toString() = v.toString()
    override fun asString() = v.toString()

    override fun isZero() = v == 0F

    override fun cmpEq(a2: Value) = (a2 is VFloat) && (v == a2.v)
    override fun cmpGt(a2: Value) = (a2 is VFloat) && (v > a2.v)
    override fun cmpGe(a2: Value) = (a2 is VFloat) && (v >= a2.v)

    override fun negate() = VFloat(-v)

    override fun plus(a2: Value) = when (a2) {
        is VInt -> VFloat(v + a2.v.toFloat())
        is VFloat -> VFloat(v + a2.v)
        is VString -> VString(asString() + a2.v)
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
    override fun toPower(a2: Value) = when (a2) {
        is VInt -> VFloat(v.pow(a2.v))
        is VFloat -> VFloat(v.pow(a2.v))
        else -> null
    }

    override fun getProp(name: String): Value? {
        when (name) {
            "asInt" -> return VInt(v.toInt())
            "asString" -> return VString(asString())
            "floor" -> return VFloat(floor(v))
            "ceil" -> return VFloat(ceil(v))
            "sqrt" -> return VFloat(sqrt(v))
        }
        return null
    }

}
