package com.dlfsystems.world

import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.world.trait.SysTrait
import com.dlfsystems.world.trait.Trait
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class World(
    val name: String = "world"
) {

    val traits: MutableMap<Uuid, Trait> = mutableMapOf()
    val traitIDs: MutableMap<String, Uuid> = mutableMapOf()

    val objs: MutableMap<Uuid, Obj> = mutableMapOf()

    fun getTrait(named: String) = traits[traitIDs[named]]
    fun getTrait(id: Uuid) = traits[id]

    fun getSysValue(c: Context, name: String): Value = getTrait("sys")!!.getProp(c, name)!!

    fun addTrait(name: String): Trait {
        if (traits.values.none { it.name == name }) {
            return (if (name == "sys") SysTrait() else Trait(name)).also {
                traits[it.id] = it
                traitIDs[it.name] = it.id
            }
        }
        throw IllegalArgumentException("Trait with id $name already exists")
    }

    fun createObj() = Obj().also { objs[it.id] = it }

    fun programVerb(traitName: String, name: String, code: String): String {
        getTrait(traitName)?.also { trait ->
            try {
                val cOut = Compiler.compile(code)
                trait.programVerb(name, cOut)
                return "programmed \$${traitName}.${name} (${cOut.code.size} words)"
            } catch (e: Exception) {
                return "compile error: $e"
            }
        }
        return "ERR: unknown trait $traitName"
    }

    fun listVerb(traitName: String, name: String): String {
        getTrait(traitName)?.also { trait ->
            trait.verbs[name]?.also { return it.getListing() }
                ?: return "ERR: verb not found $name"
        }
        return "ERR: unknown trait $traitName"
    }
}
