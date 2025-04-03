package com.dlfsystems.world

import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.VString
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.world.trait.SysTrait
import com.dlfsystems.world.trait.Trait
import com.dlfsystems.world.trait.UserTrait
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class World(
    val name: String = "world"
) {

    val traits: MutableMap<Uuid, Trait> = mutableMapOf()
    val traitIDs: MutableMap<String, Uuid> = mutableMapOf()

    val objs: MutableMap<Uuid, Obj> = mutableMapOf()

    fun getUserLogin(name: String, password: String): Obj? {
        val c = Context(this)
        getTrait("user")?.objects?.forEach { obj ->
            objs[obj]?.getProp(c, "username")?.also {
                if (it == VString(name)) {
                    objs[obj]?.getProp(c, "password")?.also {
                        if (it == VString(password)) {
                            return objs[obj]
                        }
                    }
                }
            }
        }
        return null
    }

    fun getTrait(named: String) = traits[traitIDs[named]]
    fun getTrait(id: Uuid) = traits[id]
    fun getObj(id: Uuid) = objs[id]

    fun getSysValue(c: Context, name: String): Value = getTrait("sys")!!.getProp(c, name)!!

    fun addTrait(name: String): Trait {
        if (traits.values.none { it.name == name }) {
            return when (name) {
                "sys" -> SysTrait()
                "user" -> UserTrait()
                else -> Trait(name)
            }.also {
                it.world = this
                traits[it.id] = it
                traitIDs[it.name] = it.id
            }
        }
        throw IllegalArgumentException("Trait with id $name already exists")
    }

    fun createObj() = Obj().also { objs[it.id] = it }

    fun recycleObj(objID: Uuid) {
        objs[objID]?.traits?.forEach { getTrait(it)?.removeFrom(objs[objID]!!) }
        objs.remove(objID)
    }

    fun applyTrait(traitID: Uuid, objID: Uuid) {
        traits[traitID]?.applyTo(objs[objID]!!)
    }

    fun dispelTrait(traitID: Uuid, objID: Uuid) {
        traits[traitID]?.removeFrom(objs[objID]!!)
    }

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
