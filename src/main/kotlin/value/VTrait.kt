package com.dlfsystems.value

import java.util.*

data class VTrait(val v: UUID?): Value() {
    override val type = Type.TRAIT

    override fun toString() = "\$$v"

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VTrait) && (v == a2.v)

}
