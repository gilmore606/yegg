package com.dlfsystems.value

data class VBool(val v: Boolean): Value() {
    override val type = Type.BOOL

    override fun toString() = v.toString()

    override fun isTrue() = v

    override fun cmpEq(a2: Value) = (a2 is VBool) && (v == a2.v)
    override fun cmpGt(a2: Value) = v && (a2 is VBool) && !a2.v
    override fun cmpGe(a2: Value) = v && (a2 is VBool)

    override fun plus(a2: Value) = if (a2 is VString) VString(v.toString() + a2.v) else null

}
