package com.dlfsystems.world

import com.dlfsystems.server.parser.CommandMatch
import com.dlfsystems.server.parser.Preposition
import com.dlfsystems.server.Yegg
import com.dlfsystems.util.NanoID
import com.dlfsystems.value.VList
import com.dlfsystems.value.VObj
import com.dlfsystems.value.Value
import com.dlfsystems.world.trait.Trait
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// An instance in the world.

@Serializable
@SerialName("Obj")
class Obj {

    @Serializable @JvmInline
    @SerialName("ObjID")
    value class ID(val id: String) { override fun toString() = id }

    val id = ID(NanoID.newID())
    val vThis = VObj(id)

    val traits: MutableList<Trait.ID> = mutableListOf()

    val props: MutableMap<String, Value> = mutableMapOf()

    var location: VObj = Yegg.vNullObj
    val locationObj: Obj?
        get() = location.v?.let { Yegg.world.getObj(it) }
    var contents: VList = VList()
    val contentsObjs: List<Obj>
        get() = contents.v.mapNotNull { (it as VObj).v?.let { Yegg.world.getObj(it) }}

    fun acquireTrait(trait: Trait) {
        traits.add(trait.id)
    }

    fun dispelTrait(trait: Trait) {
        trait.props.keys.forEach { props.remove(it) }
        traits.remove(trait.id)
    }

    fun getProp(name: String): Value? {
        props[name]?.also { return it }

        traits.forEach {
            Yegg.world.traits[it]?.getProp(this, name)?.also { return it }
        }
        return null
    }

    fun setProp(name: String, value: Value): Boolean {
        if (hasProp(name)) {
            props[name] = value
            return true
        }
        return false
    }

    fun hasProp(name: String): Boolean {
        if (name in props.keys) return true
        traits.forEach { traitID ->
            if (name in (Yegg.world.traits[traitID]?.props?.keys ?: listOf())) return true
        }
        return false
    }

    fun matchCommand(cmdstr: String, argstr: String, dobjstr: String, dobj: Obj?, prep: Preposition?, iobjstr: String, iobj: Obj?): CommandMatch? {
        traits.mapNotNull { Yegg.world.traits[it] }.forEach { trait ->
            trait.matchCommand(this, cmdstr, argstr, dobjstr, dobj, prep, iobjstr, iobj)?.also { return it }
        }
        return null
    }
}
