package com.dlfsystems.vm

class VMException(c: Type, m: String, lineNum: Int, charNum: Int): Exception("$c $m at line $lineNum c$charNum") {

    enum class Type {

        // Value type conflicts
        E_TYPE,

        // Variable not found
        E_VARNF,

        // Property not found
        E_PROPNF,

        // Trait not found
        E_TRAITNF,

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

}
