package com.dlfsystems.yegg.vm

class VMException(val type: Type, val m: String): Exception() {

    private var lineNum: Int = -1
    private var charNum: Int = -1

    fun withLocation(l: Int, c: Int): VMException {
        lineNum = l
        charNum = c
        return this
    }

    override fun toString() = "$type: $m  (l$lineNum c$charNum)"

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

        // Invalid object
        E_INVOBJ,

        // Invalid argument
        E_INVARG,

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
        E_USER0,
        E_USER1,
        E_USER2,
        E_USER3,
        E_USER4,
        E_USER5,
        E_USER6,
        E_USER7,
        E_USER8,
        E_USER9,
    }

}
