package com.dlfsystems.world

import com.dlfsystems.server.Yegg
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.VObj
import com.dlfsystems.value.VString
import com.dlfsystems.value.VVoid
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.world.trait.SysTrait
import com.dlfsystems.world.trait.Trait
import com.dlfsystems.world.trait.TraitID
import com.dlfsystems.world.trait.UserTrait
import kotlinx.serialization.Serializable

@Serializable
data class World(
    val name: String = "world"
) {

    private val traits: MutableMap<String, Trait> = mutableMapOf()
    private val traitIDs: MutableMap<String, String> = mutableMapOf()

    private val objs: MutableMap<String, Obj> = mutableMapOf()

    fun getUserLogin(name: String, password: String): Obj? {
        getTrait("user")?.objects?.forEach { obj ->
            objs[obj.id]?.getProp("username")?.also {
                if (it == VString(name)) {
                    objs[obj.id]?.getProp("password")?.also {
                        if (it == VString(password)) {
                            return objs[obj.id]
                        }
                    }
                }
            }
        }
        return null
    }

    fun getTrait(named: String) = traits[traitIDs[named]]
    fun getTrait(id: TraitID?) = id?.let { traits[it.id] }
    fun getObj(id: ObjID?) = id?.let { objs[it.id] }

    val sys: Trait
        get() = getTrait("sys")!!
    fun getSysValue(name: String): Value = sys.getProp(null, name) ?: VVoid

    fun addTrait(name: String): Trait {
        if (traits.values.none { it.name == name }) {
            return when (name) {
                "sys" -> SysTrait()
                "user" -> UserTrait()
                else -> Trait(name)
            }.also {
                traits[it.id.id] = it
                traitIDs[it.name] = it.id.id
            }
        }
        throw IllegalArgumentException("Trait with id $name already exists")
    }

    fun createObj() = Obj().also { objs[it.id.id] = it }

    fun destroyObj(obj: Obj) {
        obj.traits.forEach { getTrait(it)?.removeFrom(obj) }
        obj.contents.v.forEach {
            moveObj(getObj((it as VObj).v)!!, obj.location)
        }
        moveObj(obj, Yegg.vNullObj)
        objs.remove(obj.id.id)
    }

    fun moveObj(obj: Obj, newLocV: VObj) {
        val oldLoc = obj.location
        // Prevent recursive move
        var checkLoc = getObj(newLocV.v)
        while (checkLoc != null) {
            if (checkLoc.location == obj.vThis) throw IllegalArgumentException("Recursive move")
            checkLoc = getObj(checkLoc.location.v)
        }
        oldLoc.v?.also { getObj(it)!!.contents.v.removeIf { it == obj.vThis } }
        getObj(newLocV.v)?.also {
            it.contents.v.add(obj.vThis)
            obj.location = newLocV
        } ?: run {
            obj.location = Yegg.vNullObj
        }
    }

    fun applyTrait(traitID: TraitID, objID: ObjID) {
        traits[traitID.id]?.applyTo(objs[objID.id]!!)
    }

    fun dispelTrait(traitID: TraitID, objID: ObjID) {
        traits[traitID.id]?.removeFrom(objs[objID.id]!!)
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
