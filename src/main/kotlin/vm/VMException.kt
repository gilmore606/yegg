package com.dlfsystems.vm

class VMException(val type: Type, val m: String, val lineNum: Int, val charNum: Int): Exception() {

    enum class Type {

        // Value type conflicts
        E_TYPE,

        // Variable not found
        E_VARNF,

        // Property not found
        E_PROPNF,

        // Trait not found
        E_TRAITNF,

        // Func not found
        E_FUNCNF,

        // List or map accessed out of range
        E_RANGE,

        // Division by zero
        E_DIV,

        // System resource limit exceeded (stack, ticks)
        E_LIMIT,

        // Other system failures
        E_SYS,

        // User-thrown exception
        E_USER,
    }

    override fun toString() = "$type: $m (at $lineNum c$charNum)"

}
