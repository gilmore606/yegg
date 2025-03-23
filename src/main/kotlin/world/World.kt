package com.dlfsystems.world

import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.world.trait.SysTrait
import com.dlfsystems.world.trait.Trait
import kotlin.uuid.Uuid

class World {

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
            val result = Compiler.compile(code)
            result.code?.also { outcode ->
                trait.programVerb(name, outcode)
                return "programmed \$${traitName}.${name} (${outcode.size} words)"
            } ?: return "compile error: ${result.e}"
        } ?: return "ERR: unknown trait"
    }
}
