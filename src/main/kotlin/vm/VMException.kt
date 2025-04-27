package com.dlfsystems.vm

class VMException(val type: Type, val m: String): Exception() {

    private var lineNum: Int = -1
    private var charNum: Int = -1

    fun withLocation(l: Int, c: Int): VMException {
        if (lineNum == -1) {
            lineNum = l
            charNum = c
        }
        return this
    }

    override fun toString() = "$type: $m (at line $lineNum c$charNum)"

    enum class Type {

        // Value type conflicts
        E_TYPE,

        // Variable not found
        E_VARNF,

        // Property not found
        E_PROPNF,

        // Trait not found
        E_TRAITNF,

        // Verb not found
        E_VERBNF,

        // List or map accessed out of range
        E_RANGE,

        // Division by zero
        E_DIV,

        // System resource limit exceeded
        E_LIMIT,

        // Callstack recursion limit exceeded
        E_MAXREC,

        // Other system failures
        E_SYS,

        // User-thrown exception
        E_USER,
    }

}
