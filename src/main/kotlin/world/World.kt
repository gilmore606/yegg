package com.dlfsystems.yegg.world

import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.util.fail
import com.dlfsystems.yegg.value.*
import com.dlfsystems.yegg.vm.VMException.Type.E_INVARG
import com.dlfsystems.yegg.vm.VMException.Type.E_MAXREC
import com.dlfsystems.yegg.world.trait.*
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt

@Serializable
data class World(val name: String) {

    val traits: MutableMap<Trait.ID, Trait> = mutableMapOf()
    private val traitIDs: MutableMap<String, Trait.ID> = mutableMapOf()

    val objs: MutableMap<Obj.ID, Obj> = mutableMapOf()

    fun getUserLogin(name: String, password: String): Obj? {
        getTrait("user")?.objects?.forEach { obj ->
            objs[obj]?.getProp("username")?.also {
                if (it == VString(name)) {
                    objs[obj]?.getProp("password")?.also {
                        if (BCrypt.checkpw(password, it.asString())) {
                            return objs[obj]
                        }
                    }
                }
            }
        }
        return null
    }

    fun getTrait(named: String) = traits[traitIDs[named]]

    val sys: Trait
        get() = getTrait("sys")!!
    fun getSysValue(name: String): Value = sys.getProp(name) ?: VVoid
    fun getSysInt(name: String): Int = (sys.getProp(name) as VInt).v

    fun createTrait(traitName: String): Trait? {
        getTrait(traitName)?.also {
            fail(E_INVARG, "Trait with id $traitName already exists")
        } ?: run {
            return when (traitName) {
                "sys" -> SysTrait()
                "user" -> UserTrait()
                else -> Trait.NTrait(traitName)
            }.also {
                traits[it.id] = it
                traitIDs[it.name] = it.id
            }
        }
        return null
    }

    fun destroyTrait(traitName: String) {
        getTrait(traitName)?.also { trait ->
            if (trait.children.isNotEmpty()) fail(E_INVARG, "Can't destroy trait with descendant traits")
            trait.removeSelf()
            traits.remove(trait.id)
            traitIDs.remove(traitName)
        } ?: fail(E_INVARG, "No trait $traitName exists")
    }

    fun createObj() = Obj().also { objs[it.id] = it }

    fun destroyObj(obj: Obj) {
        obj.traits.forEach { traits[it]?.unapplyFrom(obj, forDestroy = true) }
        obj.contents.v.forEach {
            moveObj((it as VObj).obj()!!, obj.location)
        }
        moveObj(obj, Yegg.vNullObj)
        objs.remove(obj.id)
    }

    fun moveObj(obj: Obj, newLocV: VObj) {
        val oldLoc = obj.location
        // Prevent recursive move
        var checkLoc = newLocV.obj()
        while (checkLoc != null) {
            if (checkLoc.location == obj.vThis) fail(E_MAXREC, "Recursive move")
            checkLoc = checkLoc.location.obj()
        }
        oldLoc.obj()?.also { it.contents.v.removeIf { it == obj.vThis } }
        newLocV.obj()?.also {
            it.contents.v.add(obj.vThis)
            obj.location = newLocV
        } ?: run {
            obj.location = Yegg.vNullObj
        }
    }

    fun programVerb(traitName: String, name: String, code: String): String {
        getTrait(traitName)?.also { trait ->
            try {
                trait.programVerb(name, code)
                return "programmed \$${traitName}.${name}"
            } catch (e: Exception) {
                return "compile error: $e"
            }
        }
        return "ERR: unknown trait $traitName"
    }


}
