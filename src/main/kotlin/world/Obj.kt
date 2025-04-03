package com.dlfsystems.world

import com.dlfsystems.value.VObj
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.world.trait.Trait
import kotlinx.serialization.Serializable
import ulid.ULID

// An instance in the world.

@Serializable
class Obj {

    val id: ULID = ULID.nextULID()
    val vThis = VObj(id)

    val traits: MutableList<ULID> = mutableListOf()

    val props: MutableMap<String, Value> = mutableMapOf()

    fun acquireTrait(trait: Trait) {
        traits.add(trait.id)
    }

    fun dispelTrait(trait: Trait) {
        trait.props.keys.forEach { props.remove(it) }
        traits.remove(trait.id)
    }

    fun getProp(c: Context, name: String): Value? {
        props[name]?.also { return it }

        traits.forEach {
            c.getTrait(it)?.getProp(c, name)?.also { return it }
        }
        return null
    }

    fun setProp(c: Context, name: String, value: Value): Boolean {
        if (hasProp(c, name)) {
            props[name] = value
            return true
        }
        return false
    }

    fun hasProp(c: Context, name: String): Boolean {
        if (name in props.keys) return true
        traits.forEach { traitID ->
            if (name in (c.getTrait(traitID)?.props?.keys ?: listOf())) return true
        }
        return false
    }

}
