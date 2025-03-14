package com.dlfsystems.value

import com.dlfsystems.vm.Context
import java.util.*

data class VTrait(val v: UUID?): Value() {
    override val type = Type.TRAIT

    override fun toString() = "\$$v"

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VTrait) && (v == a2.v)

    override fun getProp(context: Context?, propname: String): Value? {
        when (propname) {
            "asString" -> v?.also { v ->
                return VString("$" + context?.world?.getTrait(v)?.name)
            } ?: return VString(toString())
        }
        return null
    }

    override fun setProp(context: Context?, propname: String, value: Value): Boolean {
        return false
    }

}
