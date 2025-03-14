package com.dlfsystems.world.thing

import com.dlfsystems.value.VThing
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import java.util.*

// An instance in the world.

class Thing {

    val id: UUID = UUID.randomUUID()
    private val vThis = VThing(id)

    val traits: MutableList<UUID> = mutableListOf()

    fun callFunc(c: Context, funcName: String): Value? {
        c.vThis = vThis
        traits.forEach { id ->
            c.getTrait(id)?.also { trait ->
                trait.callFunc(c, funcName)?.also { return it }
            }
        }
        return null
    }

}
