package com.dlfsystems.value

import com.dlfsystems.vm.Context
import java.util.*

data class VTrait(val v: UUID?): Value() {

    fun getTrait(c: Context?) = v?.let { c?.world?.getTrait(it) }

    override val type = Type.TRAIT

    override fun toString() = "\$$v"

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VTrait) && (v == a2.v)

    override fun getProp(c: Context, propname: String): Value? {
        val trait = getTrait(c)
        when (propname) {
            "asString" -> v?.also { v ->
                return VString("$" + c.world?.getTrait(v)?.name)
            } ?: return VString(toString())
        }
        return trait?.getProp(c, propname)
    }

    override fun setProp(c: Context, propname: String, value: Value): Boolean {
        // TODO: set default property
        return false
    }

}
