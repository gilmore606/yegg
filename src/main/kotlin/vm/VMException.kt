package com.dlfsystems.vm

class VMException(c: Type, m: String, lineNum: Int, charNum: Int): Exception("$c $m at line $lineNum c$charNum") {
    enum class Type {
        E_TYPE,
        E_VARNF,
        E_PROPNF,
        E_DIV,
        E_RESOURCE,
        E_SYS,
    }
}
