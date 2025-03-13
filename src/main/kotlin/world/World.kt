package com.dlfsystems.world

import com.dlfsystems.compiler.Compiler
import com.dlfsystems.trait.Trait
import com.dlfsystems.world.thing.Thing
import java.util.UUID

class World {

    val traits: MutableMap<UUID, Trait> = mutableMapOf()
    val traitIDs: MutableMap<String, UUID> = mutableMapOf()

    val things: MutableMap<UUID, Thing> = mutableMapOf()

    fun trait(named: String) = traits[traitIDs[named]]
    fun trait(id: UUID) = traits[id]

    fun createTrait(name: String): Trait {
        if (traits.values.none { it.name == name }) {
            return Trait(name).also {
                traits[it.id] = it
                traitIDs[it.name] = it.id
            }
        }
        throw IllegalArgumentException("Trait with id $name already exists")
    }

    fun createThing() = Thing().also { things[it.id] = it }

    fun programFunc(traitName: String, funcName: String, code: String): String {
        if (!traitIDs.contains(traitName)) { return "ERR: unknown trait" }
        val result = Compiler.compile(code)
        when (result) {
            is Compiler.Result.Failure -> return result.toString()
            is Compiler.Result.Success -> {
                trait(traitName)!!.programFunc(funcName, result.code)
                return "programmed \$${traitName}.${funcName} (${result.code.size} words)"
            }
        }
    }
}
