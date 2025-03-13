package com.dlfsystems.vm

import java.util.UUID

// A literal value in VM language.

sealed class Value {
    enum class Type { VOID, BOOL, INT, FLOAT, STRING, THING }
    abstract val type: Type

    open fun isTrue(): Boolean = false
    fun isFalse(): Boolean = !isTrue()
    open fun isZero(): Boolean = false

    open fun cmpEq(a2: Value): Boolean = false
    open fun cmpGt(a2: Value): Boolean = false
    open fun cmpGe(a2: Value): Boolean = false
    fun cmpLt(a2: Value): Boolean = !cmpGe(a2)
    fun cmpLe(a2: Value): Boolean = !cmpGt(a2)

    open fun plus(a2: Value): Value? = null
    open fun multiply(a2: Value): Value? = null
    open fun divide(a2: Value): Value? = null

    data class VVoid(val v: Unit = Unit): Value() {
        override val type = Type.VOID
        override fun toString() = "<VOID>"
        override fun cmpEq(a2: Value): Boolean = a2 is VVoid
    }

    data class VBool(val v: Boolean): Value() {
        override val type = Type.BOOL
        override fun isTrue() = v
        override fun toString() = v.toString()
        override fun cmpEq(a2: Value) = (a2 is VBool) && (v == a2.v)
        override fun cmpGt(a2: Value) = v && (a2 is VBool) && !a2.v
        override fun cmpGe(a2: Value) = v && (a2 is VBool)
        override fun plus(a2: Value) = if (a2 is VString) VString(v.toString() + a2.v) else null
    }

    data class VInt(val v: Int): Value() {
        override val type = Type.INT
        override fun toString() = v.toString()
        override fun isZero() = v == 0
        override fun cmpEq(a2: Value) = (a2 is VInt) && (v == a2.v)
        override fun cmpGt(a2: Value) = (a2 is VInt) && (v > a2.v)
        override fun cmpGe(a2: Value) = (a2 is VInt) && (v >= a2.v)
        override fun plus(a2: Value) = when (a2) {
            is VInt -> VInt(v + a2.v)
            is VFloat -> VFloat(v.toFloat() + a2.v)
            is VString -> VString(v.toString() + a2.v)
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
    }

    data class VFloat(val v: Float): Value() {
        override val type = Type.FLOAT
        override fun toString() = v.toString()
        override fun isZero() = v == 0F
        override fun cmpEq(a2: Value) = (a2 is VFloat) && (v == a2.v)
        override fun cmpGt(a2: Value) = (a2 is VFloat) && (v > a2.v)
        override fun cmpGe(a2: Value) = (a2 is VFloat) && (v >= a2.v)
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
    }

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
    }

    data class VThing(val v: UUID?): Value() {
        override val type = Type.THING
        override fun isTrue() = v != null
        override fun toString() = "#$v"
        override fun cmpEq(a2: Value) = (a2 is VThing) && (v == a2.v)
        override fun plus(a2: Value) = if (a2 is VString) VString(v.toString() + a2.v) else null
    }
}
