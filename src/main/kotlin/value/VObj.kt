package com.dlfsystems.value

import com.dlfsystems.vm.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class VObj(val v: Uuid?): Value() {

    @SerialName("yType")
    override val type = Type.OBJ

    override fun toString() = "#$v"
    override fun asString() = "OBJ" // TODO: use name from passed context?

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VObj) && (v == a2.v)

    override fun plus(a2: Value) = if (a2 is VString) VString(v.toString() + a2.v) else null

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "asString" -> return propAsString()
            "traits" -> return propTraits(c)
        }
        return v?.let { c.getObj(it)?.getProp(c, name) }
    }

    override fun setProp(c: Context, name: String, value: Value): Boolean {
        c.getObj(v)?.also { obj ->
            return obj.setProp(c, name, value)
        }
        return false
    }


    // Custom props

    private fun propAsString() = VString(toString())

    private fun propTraits(c: Context) = VList((
        v?.let { c.getObj(it)?.traits?.map { VTrait(it) } } ?: mutableListOf()
    ).toMutableList())

    // Custom verbs

}
