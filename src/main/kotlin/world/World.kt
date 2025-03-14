package com.dlfsystems.world

import com.dlfsystems.compiler.Compiler
import com.dlfsystems.world.trait.SysTrait
import com.dlfsystems.world.trait.Trait
import java.util.UUID

class World {

    val traits: MutableMap<UUID, Trait> = mutableMapOf()
    val traitIDs: MutableMap<String, UUID> = mutableMapOf()

    val objs: MutableMap<UUID, Obj> = mutableMapOf()

    fun getTrait(named: String) = traits[traitIDs[named]]
    fun getTrait(id: UUID) = traits[id]

    fun createTrait(name: String): Trait {
        if (traits.values.none { it.name == name }) {
            return (if (name == "sys") SysTrait() else Trait(name)).also {
                traits[it.id] = it
                traitIDs[it.name] = it.id
            }
        }
        throw IllegalArgumentException("Trait with id $name already exists")
    }

    fun createObj() = Obj().also { objs[it.id] = it }

    fun programFunc(traitName: String, funcName: String, code: String): String {
        if (!traitIDs.contains(traitName)) { return "ERR: unknown trait" }
        val result = Compiler.compile(code)
        when (result) {
            is Compiler.Result.Failure -> return result.toString()
            is Compiler.Result.Success -> {
                getTrait(traitName)!!.programFunc(funcName, result.code)
                return "programmed \$${traitName}.${funcName} (${result.code.size} words)"
            }
        }
    }
}
