package com.dlfsystems.world.trait

import com.dlfsystems.server.Yegg
import com.dlfsystems.server.parser.Command
import com.dlfsystems.server.parser.Command.Arg
import com.dlfsystems.server.parser.CommandMatch
import com.dlfsystems.server.parser.Preposition
import com.dlfsystems.util.NanoID
import com.dlfsystems.util.matchesWildcard
import com.dlfsystems.value.*
import com.dlfsystems.vm.Context
import com.dlfsystems.world.Obj
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// A collection of verbs and props, which can apply to an Obj.

@Serializable
sealed class Trait(val name: String) {

    @Serializable
    @SerialName("NTrait")
    // A "normal" dynamically defined trait.  We need this subclass for serialization.
    class NTrait(private val n: String) : Trait(n)

    @Serializable @JvmInline
    @SerialName("TraitID")
    value class ID(val id: String) { override fun toString() = id }

    val id = ID(NanoID.newID())
    val vTrait = VTrait(id)

    val traits: MutableList<ID> = mutableListOf()

    val commands: MutableSet<Command> = mutableSetOf()
    val verbs: MutableMap<String, Verb> = mutableMapOf()
    val props: MutableMap<String, Value> = mutableMapOf()

    val objects: MutableSet<Obj.ID> = mutableSetOf()

    fun applyTo(obj: Obj) {
        obj.traits.forEach {
            if (Yegg.world.traits[it]?.hasTrait(this.id) == true)
                throw IllegalArgumentException("obj already inherits trait")
        }
        objects.add(obj.id)
        obj.acquireTrait(this)
    }

    fun removeFrom(obj: Obj) {
        objects.remove(obj.id)
        obj.dispelTrait(this)
    }

    fun hasTrait(trait: ID): Boolean = (trait in traits) ||
            (traits.firstOrNull { Yegg.world.traits[it]?.hasTrait(trait) ?: false } != null)

    fun setCommand(command: Command) {
        commands.removeIf { it.verb == command.verb }
        commands.add(command)
    }

    fun removeCommand(spec: String) {
        commands.removeIf { it.spec == spec }
    }

    fun getVerb(name: String): Verb? = verbs[name]

    fun programVerb(verbName: String, source: String) {
        verbs[verbName]?.also {
            it.program(source)
        } ?: run {
            verbs[verbName] = Verb(verbName).apply { program(source) }
        }
    }

    fun removeVerb(verbName: String) {
        verbs.remove(verbName)
    }

    open fun getProp(obj: Obj?, propName: String): Value? {
        return when (propName) {
            "objects" -> return VList.make(objects.mapNotNull { Yegg.world.getObj(it)?.vThis })
            else -> props.getOrDefault(propName, null)
        }
    }

    open fun setProp(propName: String, value: Value): Boolean {
        props[propName] = value
        return true
    }

    open fun callStaticVerb(c: Context, verbName: String, args: List<Value>): Value? = null

    fun matchCommand(obj: Obj?, cmdstr: String, argstr: String, dobjstr: String, dobj: Obj?, prep: Preposition?, iobjstr: String, iobj: Obj?): CommandMatch? {
        fun matchArg(argType: Arg?, argString: String, matchedObj: Obj?): Value? =
            when (argType) {
                Arg.THIS -> if (obj != matchedObj) null else VVoid
                Arg.STRING -> VString(argString)
                Arg.ANY -> matchedObj?.vThis
                null -> if (argString.isNotBlank()) null else VVoid
            }

        for (cmd in commands) {
            if (cmd.names.any { cmdstr.matchesWildcard(it) }) {
                var a1: Value = VVoid
                var a2: Value = VVoid
                if (cmd.prep == null) {
                    a1 = matchArg(cmd.dobj, argstr, dobj) ?: continue
                } else if (cmd.prep == prep) {
                    a1 = matchArg(cmd.dobj, dobjstr, dobj) ?: continue
                    a2 = matchArg(cmd.iobj, iobjstr, iobj) ?: continue
                }
                return CommandMatch(cmd.verb, this, obj, buildList {
                    if (a1 != VVoid) add(a1)
                    if (a2 != VVoid) add(a2)
                })
            }
        }

        traits.mapNotNull { Yegg.world.traits[it] }.forEach { parent ->
            parent.matchCommand(obj, cmdstr, argstr, dobjstr, dobj, prep, iobjstr, iobj)?.also { return it }
        }

        return null
    }

}
