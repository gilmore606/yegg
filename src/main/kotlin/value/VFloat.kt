package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException
import com.dlfsystems.vm.VMException.Type.E_INVARG
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.*

@Serializable
@SerialName("VFloat")
data class VFloat(val v: Float): Value() {
    override fun equals(other: Any?) = other is VFloat && v == other.v
    override fun hashCode() = javaClass.hashCode()

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

    override fun getProp(name: String) = when (name) {
        "asInt" -> VInt(v.toInt())
        "asString" -> VString(asString())
        "floor" -> VFloat(floor(v))
        "ceil" -> VFloat(ceil(v))
        "sqrt" -> VFloat(sqrt(v))
        "abs" -> VFloat(abs(v))
        else -> null
    }

    override fun callStaticVerb(c: Context, name: String, args: List<Value>) = when (name) {
        "atMost" -> verbAtMost(args)
        "atLeast" -> verbAtLeast(args)
        else -> null
    }

    private fun verbAtMost(args: List<Value>): VFloat {
        requireArgCount(args, 1, 1)
        return when (args[0].type) {
            Type.FLOAT -> if ((args[0] as VFloat).v >= v) this else args[0] as VFloat
            Type.INT -> if ((args[0] as VInt).v >= v) this else VFloat((args[0] as VInt).v.toFloat())
            else -> throw VMException(E_INVARG, "${args[0].type} is not numeric")
        }
    }

    private fun verbAtLeast(args: List<Value>): VFloat {
        requireArgCount(args, 1, 1)
        return when (args[0].type) {
            Type.FLOAT -> if ((args[0] as VFloat).v <= v) this else args[0] as VFloat
            Type.INT -> if ((args[0] as VInt).v <= v) this else VFloat((args[0] as VInt).v.toFloat())
            else -> throw VMException(E_INVARG, "${args[0].type} is not numeric")
        }
    }

}
