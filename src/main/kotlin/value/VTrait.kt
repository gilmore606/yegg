@file:Suppress("NOTHING_TO_INLINE")

package com.dlfsystems.yegg.value

import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.vm.Context
import com.dlfsystems.yegg.world.trait.Trait
import com.dlfsystems.yegg.world.trait.Verb
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VTrait")
data class VTrait(val v: Trait.ID?): Value() {
    override fun equals(other: Any?) = other is VTrait && v == other.v
    override fun hashCode() = javaClass.hashCode()

    @SerialName("yType")
    override val type = Type.TRAIT

    inline fun trait() = Yegg.world.traits[v]

    override fun toString() = "$" + (v?.let { it.trait()?.name } ?: "null")
    override fun asString() = toString()

    override fun isTrue() = v != null

    override fun cmpEq(a2: Value) = (a2 is VTrait) && (v == a2.v)

    override fun getProp(name: String) = when (name) {
        "asString" -> propAsString()
        "parents" -> propParents()
        "children" -> propChildren()
        else -> trait()?.getProp(name)
    }

    override fun setProp(name: String, value: Value) =
        trait()?.setProp(name, value) ?: false

    private fun propAsString() = v?.let { v ->
        VString("$" + trait()?.name)
    } ?: VString(asString())

    private fun propParents() = v?.let { v ->
        VList.make(trait()?.parents?.map { VTrait(it) } ?: listOf())
    } ?: VList.make(listOf())

    private fun propChildren() = v?.let { v ->
        VList.make(trait()?.children?.map { VTrait(it) } ?: listOf())
    } ?: VList.make(listOf())

    override fun callStaticVerb(c: Context, name: String, args: List<Value>): Value? {
        return trait()?.callStaticVerb(c, name, args)
    }

    override fun getVerb(name: String): Verb? = trait()?.getVerb(name)

}
