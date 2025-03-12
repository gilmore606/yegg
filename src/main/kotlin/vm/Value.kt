package com.dlfsystems.vm

import java.util.UUID

// A literal value in VM language.

sealed class Value {
    enum class Type { VOID, BOOL, INT, FLOAT, STR, THING }
    abstract val type: Type
    open fun isTrue(): Boolean = false
    open fun isFalse(): Boolean = !isTrue()

    data class VVoid(val v: Boolean = true): Value() {
        override val type = Type.VOID
        override fun toString() = "<VOID>"
    }

    data class VBool(val v: Boolean): Value() {
        override val type = Type.BOOL
        override fun isTrue() = v
        override fun toString() = v.toString()
    }

    data class VInt(val v: Int): Value() {
        override val type = Type.INT
        override fun toString() = v.toString()
    }

    data class VFloat(val v: Float): Value() {
        override val type = Type.FLOAT
        override fun toString() = v.toString()
    }

    data class VString(val v: String): Value() {
        override val type = Type.STR
        override fun toString() = "\"$v\""
    }

    data class VThing(val v: UUID?): Value() {
        override val type = Type.THING
        override fun isTrue() = v != null
        override fun toString() = "#$v"
    }
}
