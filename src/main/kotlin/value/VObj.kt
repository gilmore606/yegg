package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.world.Obj
import com.dlfsystems.world.ObjID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VObj(val v: ObjID?): Value() {

    @SerialName("yType")
    override val type = Type.OBJ

    override fun toString() = "#${v.toString().takeLast(5)}"
    override fun asString() = "OBJ" // TODO: use name from passed context?

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VObj) && (v == a2.v)

    override fun plus(a2: Value) = if (a2 is VString) VString(v.toString() + a2.v) else null

    override fun getProp(c: Context, name: String): Value? {
        c.getObj(v)?.also { obj ->
            when (name) {
                "asString" -> return propAsString()
                "traits" -> return propTraits(obj)
                "location" -> return obj.location
                "contents" -> return obj.contents
            }
            return obj.getProp(c, name)
        }
        throw IllegalArgumentException("Invalid obj")
    }

    override fun setProp(c: Context, name: String, value: Value): Boolean {
        c.getObj(v)?.also { obj ->
            return obj.setProp(c, name, value)
        }
        return false
    }


    // Custom props

    private fun propAsString() = VString(toString())

    private fun propTraits(obj: Obj) = VList(obj.traits.map { VTrait(it) }.toMutableList())

    // Custom verbs

}
