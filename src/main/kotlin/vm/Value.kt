package com.dlfsystems.vm

import java.util.UUID

// A literal value in VM language.

data class Value(val type: Type, val boolV: Boolean? = null, val intV: Int? = null, val floatV: Float? = null, val stringV: String? = null, val objV: UUID? = null) {
    enum class Type { VOID, BOOL, INT, FLOAT, STRING, OBJECT }

    override fun toString() = if (type == Type.VOID) "VOID"
                              else boolV?.toString() ?: intV?.toString() ?: floatV?.toString() ?: stringV ?: objV?.toString() ?: "null"

    fun isTrue() = (type == Type.BOOL && boolV == true) || (type == Type.OBJECT && objV != null)
    fun isFalse() = !isTrue()

}

inline fun voidV() = Value(Value.Type.VOID)
inline fun intV(v: Int?) = Value(Value.Type.INT,  intV = v)
inline fun boolV(v: Boolean?) = Value(Value.Type.BOOL, boolV = v)
inline fun floatV(v: Float?) = Value(Value.Type.FLOAT,  floatV = v)
inline fun stringV(v: String?) = Value(Value.Type.STRING, stringV = v)
inline fun objectV(v: UUID?) = Value(Value.Type.OBJECT, objV = v)
