package com.dlfsystems.world.trait

import com.dlfsystems.server.Yegg
import com.dlfsystems.app.Log
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.server.Command
import com.dlfsystems.server.Command.Arg
import com.dlfsystems.server.CommandMatch
import com.dlfsystems.server.Preposition
import com.dlfsystems.util.matchesWildcard
import com.dlfsystems.value.*
import com.dlfsystems.vm.Context
import com.dlfsystems.world.Obj
import com.dlfsystems.world.ObjID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// A collection of verbs and props, which can apply to an Obj.

@Serializable
@SerialName("TraitID")
data class TraitID(val id: String) { override fun toString() = id }

@Serializable
sealed class Trait(val name: String) {

    val id = TraitID(Yegg.newID())
    val vTrait = VTrait(id)

    val traits: MutableList<TraitID> = mutableListOf()

    val commands: MutableSet<Command> = mutableSetOf()
    val verbs: MutableMap<String, Verb> = mutableMapOf()
    val props: MutableMap<String, Value> = mutableMapOf()

    val objects: MutableSet<ObjID> = mutableSetOf()

    fun applyTo(obj: Obj) {
        obj.traits.forEach {
            if (Yegg.world.getTrait(it)?.hasTrait(this.id) == true)
                throw IllegalArgumentException("obj already inherits trait")
        }
        objects.add(obj.id)
        obj.acquireTrait(this)
    }

    fun removeFrom(obj: Obj) {
        objects.remove(obj.id)
        obj.dispelTrait(this)
    }

    fun hasTrait(trait: TraitID): Boolean = (trait in traits) ||
            (traits.firstOrNull { Yegg.world.getTrait(it)?.hasTrait(trait) ?: false } != null)

    fun setCommand(command: Command) {
        commands.removeIf { it.verb == command.verb }
        commands.add(command)
    }

    fun removeCommand(spec: String) {
        commands.removeIf { it.spec == spec }
    }

    fun programVerb(verbName: String, cOut: Compiler.Result) {
        verbs[verbName]?.also {
            it.program(cOut)
        } ?: run {
            verbs[verbName] = Verb(verbName).apply { program(cOut) }
        }
    }

    fun removeVerb(verbName: String) {
        verbs.remove(verbName)
    }

    open fun getProp(obj: Obj?, propName: String): Value? {
        return when (propName) {
            "objects" -> return VList(objects.mapNotNull { Yegg.world.getObj(it)?.vThis }.toMutableList())
            else -> props.getOrDefault(propName, null)
        }
    }

    open fun setProp(propName: String, value: Value): Boolean {
        props[propName] = value
        return true
    }

    open fun callVerb(c: Context, verbName: String, args: List<Value>): Value? {
        verbs[verbName]?.also {
            Log.i("static verb call: \$$name.$verbName($args)")
            return it.call(c, c.vThis, vTrait, args)
        }
        return null
    }

    fun matchCommand(obj: Obj?, cmdstr: String, argstr: String, dobjstr: String, dobj: Obj?, prep: Preposition?, iobjstr: String, iobj: Obj?): CommandMatch? {
        fun matchArg(t: Obj?, arg: Arg?, s: String, o: Obj?): Value? =
            when (arg) {
                Arg.THIS -> if (t != o) null else VVoid
                Arg.STRING -> VString(s)
                Arg.ANY -> o?.vThis
                null -> if (s.isNotBlank()) null else VVoid
            }

        for (cmd in commands) {
            if (cmd.names.any { cmdstr.matchesWildcard(it) }) {
                var a1: Value = VVoid
                var a2: Value = VVoid
                if (cmd.prep == null) {
                    a1 = matchArg(obj, cmd.dobj, argstr, dobj) ?: continue
                } else if (cmd.prep == prep) {
                    a1 = matchArg(obj, cmd.dobj, dobjstr, dobj) ?: continue
                    a2 = matchArg(obj, cmd.iobj, iobjstr, iobj) ?: continue
                }
                return CommandMatch(cmd.verb, this, obj, buildList {
                    if (a1 != VVoid) add(a1)
                    if (a2 != VVoid) add(a2)
                })
            }
        }

        traits.mapNotNull { Yegg.world.getTrait(it) }.forEach { parent ->
            parent.matchCommand(obj, cmdstr, argstr, dobjstr, dobj, prep, iobjstr, iobj)?.also { return it }
        }

        return null
    }

}

@Serializable
@SerialName("NTrait")
// A "normal" dynamically defined trait.  We need this subclass for serialization.
class NTrait(private val n: String) : Trait(n)
