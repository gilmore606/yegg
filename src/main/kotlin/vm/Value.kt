package com.dlfsystems.vm

// A literal value in VM language.

data class Value(val type: Type, val boolValue: Boolean? = null, val intValue: Int? = null, val floatValue: Float? = null, val stringValue: String? = null) {
    enum class Type { VOID, BOOL, INT, FLOAT, STRING }

    override fun toString() = if (type == Type.VOID) "VOID" else boolValue?.toString() ?: intValue?.toString() ?: floatValue?.toString() ?: stringValue ?: "null"

    fun greaterThan(v2: Value): Boolean {
        if (type != v2.type) return false
        return when (type) {
            Type.BOOL -> (boolValue!! > v2.boolValue!!)
            Type.INT -> (intValue!! > v2.intValue!!)
            Type.FLOAT -> (floatValue!! > v2.floatValue!!)
            Type.STRING -> (stringValue!! > v2.stringValue!!)
            else -> false
        }
    }

    fun greaterOrEqual(v2: Value): Boolean {
        if (type != v2.type) return false
        return when (type) {
            Type.BOOL -> (boolValue!! >= v2.boolValue!!)
            Type.INT -> (intValue!! >= v2.intValue!!)
            Type.FLOAT -> (floatValue!! >= v2.floatValue!!)
            Type.STRING -> (stringValue!! >= v2.stringValue!!)
            else -> false
        }
    }
}
