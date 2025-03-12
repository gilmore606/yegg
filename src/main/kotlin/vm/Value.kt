package com.dlfsystems.vm

// A literal value in VM language.

data class Value(val type: Type, val boolV: Boolean? = null, val intV: Int? = null, val floatV: Float? = null, val stringV: String? = null) {
    enum class Type { VOID, BOOL, INT, FLOAT, STRING }

    override fun toString() = if (type == Type.VOID) "VOID" else boolV?.toString() ?: intV?.toString() ?: floatV?.toString() ?: stringV ?: "null"

    fun isTrue() = type == Type.BOOL && boolV == true
    fun isFalse() = type == Type.BOOL && boolV == false

    fun greaterThan(v2: Value): Boolean {
        if (type != v2.type) return false
        return when (type) {
            Type.BOOL -> (boolV!! > v2.boolV!!)
            Type.INT -> (intV!! > v2.intV!!)
            Type.FLOAT -> (floatV!! > v2.floatV!!)
            Type.STRING -> (stringV!! > v2.stringV!!)
            else -> false
        }
    }

    fun greaterOrEqual(v2: Value): Boolean {
        if (type != v2.type) return false
        return when (type) {
            Type.BOOL -> (boolV!! >= v2.boolV!!)
            Type.INT -> (intV!! >= v2.intV!!)
            Type.FLOAT -> (floatV!! >= v2.floatV!!)
            Type.STRING -> (stringV!! >= v2.stringV!!)
            else -> false
        }
    }
}

inline fun voidValue() = Value(Value.Type.VOID)
inline fun intValue(v: Int) = Value(Value.Type.INT,  intV = v)
inline fun boolValue(v: Boolean) = Value(Value.Type.BOOL, boolV = v)
inline fun floatValue(v: Float) = Value(Value.Type.FLOAT,  floatV = v)
inline fun stringValue(v: String) = Value(Value.Type.STRING, stringV = v)
