package com.dlfsystems.value

data class VVoid(val v: Unit = Unit): Value() {
    override val type = Type.VOID

    override fun toString() = "<VOID>"

    override fun cmpEq(a2: Value): Boolean = a2 is VVoid

}
