package com.dlfsystems.value

import com.dlfsystems.vm.Context
import kotlin.uuid.Uuid

data class VTrait(val v: Uuid?): Value() {

    fun getTrait(c: Context?) = v?.let { c?.world?.getTrait(it) }

    override val type = Type.TRAIT

    override fun toString() = "\$$v"
    override fun asString() = "\$TRAIT" // TODO: get from context

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VTrait) && (v == a2.v)

    override fun getProp(c: Context, name: String): Value? {
        val trait = getTrait(c)
        when (name) {
            "asString" -> return propAsString(c)
        }
        return trait?.getProp(c, name)
    }

    override fun setProp(c: Context, name: String, value: Value): Boolean {
        val trait = getTrait(c)
        return trait?.setProp(c, name, value) ?: false
    }


    // Custom props

    private fun propAsString(c: Context) = v?.let { v ->
        VString("$" + c.world.getTrait(v)?.name)
    } ?: VString(asString())

    // Custom verbs

    override fun callVerb(c: Context, name: String, args: List<Value>): Value? {
        val trait = getTrait(c)
        return trait?.callVerb(c, name, args)
    }
}
