@file:Suppress("NOTHING_TO_INLINE")

package com.dlfsystems.yegg.world

import com.dlfsystems.yegg.server.parser.CommandMatch
import com.dlfsystems.yegg.server.parser.Preposition
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.util.NanoID
import com.dlfsystems.yegg.util.fail
import com.dlfsystems.yegg.value.VList
import com.dlfsystems.yegg.value.VObj
import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.vm.VMException.Type.E_INVARG
import com.dlfsystems.yegg.world.trait.Trait
import com.dlfsystems.yegg.world.trait.Verb
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// An instance in the world.

@Serializable
@SerialName("Obj")
class Obj {

    @Serializable @JvmInline
    @SerialName("ObjID")
    value class ID(val id: String) {
        override fun toString() = id
        inline fun obj() = Yegg.world.objs[this]
    }

    val id = ID(NanoID.newID())
    val vThis = VObj(id)

    val traits: MutableList<Trait.ID> = mutableListOf()

    // One entry per property, from all traits.  If value is null, default comes from traitID.
    val props: MutableMap<String, Propval> = mutableMapOf()

    var location: VObj = Yegg.vNullObj
    val locationObj: Obj?
        get() = location.v?.obj()
    var contents: VList = VList()
    val contentsObjs: List<Obj>
        get() = contents.v.mapNotNull { (it as VObj).obj() }

    // Traits

    // Add trait to this object.
    fun addTrait(trait: Trait) {
        traits.forEach {
            if (it.trait()!!.inheritsTrait(trait.id)) fail(E_INVARG, "obj already has trait")
        }
        traits.add(trait.id)
        trait.applyTo(this)
    }

    // Remove trait from this object.
    fun removeTrait(trait: Trait) {
        if (!traits.contains(trait.id)) fail(E_INVARG, "obj doesn't have trait")
        traits.remove(trait.id)
        trait.unapplyFrom(this)
    }

    fun inheritsTrait(traitID: Trait.ID): Boolean {
        if (traitID in traits) return true
        for (t in traits) {
            if (t.trait()!!.inheritsTrait(traitID)) return true
        }
        return false
    }

    fun isPlayer() = Yegg.world.getTrait("player")?.let { inheritsTrait(it.id) } ?: false

    // Props

    fun hasProp(propName: String) = props.containsKey(propName)

    fun addProp(propName: String, traitID: Trait.ID) {
        props[propName] = Propval(traitID)
    }

    fun removeProp(propName: String) {
        props.remove(propName)
    }

    fun getProp(propName: String) = when (propName) {
        "location" -> location
        "contents" -> contents
        "traits" -> VList.make(traits.map { it.trait()!!.vTrait })
        else -> props[propName]?.get(propName)
    }

    fun setProp(propName: String, value: Value): Boolean =
        props[propName]?.let { it.v = value ; true } == true

    fun clearProp(propName: String): Boolean =
        props[propName]?.let { it.v = null ; true } == true

    // Verbs

    fun getVerb(name: String): Verb? {
        for (t in traits) {
            t.trait()!!.getVerb(name)?.also { return it }
        }
        return null
    }

    // Commands

    fun matchCommand(cmdstr: String, argstr: String, dobjstr: String, dobj: Obj?, prep: Preposition?, iobjstr: String, iobj: Obj?): CommandMatch? {
        traits.mapNotNull { it.trait() }.forEach { trait ->
            trait.matchCommand(this, cmdstr, argstr, dobjstr, dobj, prep, iobjstr, iobj)?.also { return it }
        }
        return null
    }

}
