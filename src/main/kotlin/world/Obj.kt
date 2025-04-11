package com.dlfsystems.world

import com.dlfsystems.server.Yegg
import com.dlfsystems.value.VList
import com.dlfsystems.value.VObj
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.world.trait.Trait
import com.dlfsystems.world.trait.TraitID
import kotlinx.serialization.Serializable

// An instance in the world.

@Serializable
data class ObjID(val id: String) { override fun toString() = id }

@Serializable
class Obj {
    val id = ObjID(Yegg.newID())
    val vThis = VObj(id)

    val traits: MutableList<TraitID> = mutableListOf()

    val props: MutableMap<String, Value> = mutableMapOf()

    var location: VObj = Yegg.vNullObj
    var contents: VList = VList()

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
            Yegg.world.getTrait(it)?.getProp(name)?.also { return it }
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
            if (name in (Yegg.world.getTrait(traitID)?.props?.keys ?: listOf())) return true
        }
        return false
    }

}
