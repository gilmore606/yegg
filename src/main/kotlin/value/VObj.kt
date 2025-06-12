@file:Suppress("NOTHING_TO_INLINE")

package com.dlfsystems.value

import com.dlfsystems.server.Yegg
import com.dlfsystems.util.fail
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException
import com.dlfsystems.world.Obj
import com.dlfsystems.vm.VMException.Type.E_PROPNF
import com.dlfsystems.vm.VMException.Type.E_INVOBJ
import com.dlfsystems.world.trait.Verb
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VObj")
data class VObj(val v: Obj.ID?): Value() {
    override fun equals(other: Any?) = other is VObj && v == other.v
    override fun hashCode() = javaClass.hashCode()

    @SerialName("yType")
    override val type = Type.OBJ

    inline fun obj() = Yegg.world.objs[v]

    override fun toString() = "#${v.toString().takeLast(5)}"
    override fun asString() = "OBJ" // TODO: use name

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VObj) && (v == a2.v)

    override fun plus(a2: Value) = if (a2 is VString) VString(v.toString() + a2.v) else null

    override fun getProp(name: String): Value? {
        obj()?.also { obj ->
            when (name) {
                "asString" -> return propAsString()
                "traits" -> return propTraits(obj)
                "location" -> return obj.location
                "contents" -> return obj.contents
            }
            return obj.getProp(name)
        }
        throw VMException(E_INVOBJ, "Invalid obj")
    }

    override fun setProp(name: String, value: Value): Boolean {
        obj()?.also { obj ->
            return obj.setProp(name, value)
        }
        return false
    }

    private fun propAsString() = VString(toString())

    private fun propTraits(obj: Obj) = VList.make(obj.traits.mapNotNull { it.trait()?.vTrait })

    override fun callStaticVerb(c: Context, name: String, args: List<Value>) = when (name) {
        "hasProp" -> verbHasProp(args)
        "clearProp" -> verbClearProp(args)
        else -> null
    }

    private fun verbHasProp(args: List<Value>): VBool {
        requireArgCount(args, 1, 1)
        return VBool(obj()?.hasProp(args[0].asString()) == true)
    }

    private fun verbClearProp(args: List<Value>): VVoid {
        requireArgCount(args, 1, 1)
        obj()?.also { obj ->
            if (!obj.hasProp(args[0].asString())) fail(E_PROPNF, "prop not found")
            obj.clearProp(args[0].asString())
        } ?: fail(E_INVOBJ, "cannot clear prop on invalid obj")
        return VVoid
    }

    override fun getVerb(name: String): Verb? = obj()?.getVerb(name)

}
