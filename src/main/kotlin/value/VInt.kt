package com.dlfsystems.yegg.value

import com.dlfsystems.yegg.vm.Context
import com.dlfsystems.yegg.vm.VMException
import com.dlfsystems.yegg.vm.VMException.Type.E_INVARG
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.*

@Serializable
@SerialName("VInt")
data class VInt(val v: Int): Value() {

    override fun equals(other: Any?) = other is VInt && v == other.v
    override fun hashCode() = javaClass.hashCode()

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

    override fun getProp(name: String) = when (name) {
        "asFloat" -> VFloat(v.toFloat())
        "asString" -> VString(asString())
        "isEven" -> VBool(v % 2 == 0)
        "isOdd" -> VBool(v % 2 != 0)
        "sqrt" -> VFloat(sqrt(v.toFloat()))
        "abs" -> VInt(abs(v))
        else -> null
    }

    override fun callStaticVerb(c: Context, name: String, args: List<Value>) = when (name) {
        "atMost" -> verbAtMost(args)
        "atLeast" -> verbAtLeast(args)
        else -> null
    }

    private fun verbAtMost(args: List<Value>): VInt {
        requireArgCount(args, 1, 1)
        return when (args[0].type) {
            Type.INT -> if ((args[0] as VInt).v >= v) this else args[0] as VInt
            Type.FLOAT -> if ((args[0] as VFloat).v >= v) this else VInt(floor((args[0] as VFloat).v).toInt())
            else -> throw VMException(E_INVARG, "${args[0].type} is not numeric")
        }
    }

    private fun verbAtLeast(args: List<Value>): VInt {
        requireArgCount(args, 1, 1)
        return when (args[0].type) {
            Type.INT -> if ((args[0] as VInt).v <= v) this else args[0] as VInt
            Type.FLOAT -> if ((args[0] as VFloat).v <= v) this else VInt(ceil((args[0] as VFloat).v).toInt())
            else -> throw VMException(E_INVARG, "${args[0].type} is not numeric")
        }
    }

}
