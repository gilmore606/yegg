package com.dlfsystems.vm

// A literal value in VM language.

data class Value(val type: Type, val boolValue: Boolean? = null, val intValue: Int? = null, val floatValue: Float? = null, val stringValue: String? = null) {
    enum class Type { VOID, BOOL, INT, FLOAT, STRING }

    override fun toString() = if (type == Type.VOID) "VOID" else boolValue?.toString() ?: intValue?.toString() ?: floatValue?.toString() ?: stringValue ?: "null"
}
