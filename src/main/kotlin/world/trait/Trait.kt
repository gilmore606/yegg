@file:Suppress("NOTHING_TO_INLINE")

package com.dlfsystems.world.trait

import com.dlfsystems.server.Log
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
import com.dlfsystems.world.Propval
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
    value class ID(val id: String) {
        override fun toString() = id
        inline fun trait() = Yegg.world.traits[this]
    }

    val id = ID(NanoID.newID())
    val vTrait = VTrait(id)

    // Traits we inherit from.
    val parents: MutableList<ID> = mutableListOf()
    // Traits which inherit from us.
    val children: MutableList<ID> = mutableListOf()

    val commands: MutableSet<Command> = mutableSetOf()
    val verbs: MutableMap<String, Verb> = mutableMapOf()

    // All props we define directly OR inherit.
    val props: MutableMap<String, Propval> = mutableMapOf()

    // All objects which inherit this trait directly.
    val objects: MutableSet<Obj.ID> = mutableSetOf()


    // Execute action on all objects inheriting this trait (directly or transitively).
    private fun forEachDescendantObj(action: (Obj)->Unit) {
        objects.forEach { action.invoke(it.obj()!!) }
        children.forEach {
            it.trait()!!.forEachDescendantObj(action)
        }
    }

    // Execute action on this trait, and all traits inheriting this trait (directly or transitively).
    private fun forEachDescendantTrait(action: (Trait)->Unit) {
        action.invoke(this)
        children.forEach {
            it.trait()!!.forEachDescendantTrait(action)
        }
    }

    // Traits

    // Clean up everything about this trait, prior to destroying it.
    fun removeSelf() {
        forEachDescendantObj { obj ->
            obj.removeTrait(this)
        }
        parents.forEach { it.trait()!!.children.remove(id) }
    }

    // Add parent trait to this trait.
    fun addTrait(parent: Trait) {
        parents.forEach {
            if (it.trait()!!.inheritsTrait(parent.id)) throw IllegalArgumentException("trait already inherits $parent")
        }
        parents.add(parent.id)
        parent.children.add(id)
        // Add parent props to this trait
        parent.props.forEach { (name, parentval) ->
            if (!props.containsKey(name)) props[name] = Propval(parentval.traitID)
        }
        // Add parent props to all inheriting objects
        forEachDescendantObj { obj ->
            parent.props.forEach { (name, parentval) ->
                if (!obj.props.containsKey(name)) {
                    obj.addProp(name, parentval.traitID)
                }
            }
        }
    }

    // Remove parent trait from this trait.
    fun removeTrait(parent: Trait) {
        if (parent.id !in parents) throw IllegalArgumentException("trait does not have trait")
        parents.remove(parent.id)
        parent.children.remove(id)
        // Remove orphaned props from this trait (and inheriting objects)
        parent.props.keys.forEach { name ->
            props[name]?.also { propval ->
                if (!this.inheritsTrait(propval.traitID)) {
                    props.remove(name)
                    forEachDescendantObj { obj ->
                        obj.removeProp(name)
                    }
                }
            }
        }
    }

    fun inheritsTrait(otrait: ID): Boolean {
        if (otrait == id) return true
        parents.forEach { if (it.trait()!!.inheritsTrait(otrait)) return true }
        return false
    }

    // Objects

    fun applyTo(obj: Obj) {
        objects.add(obj.id)
        props.forEach { (name, propval) ->
            if (!obj.props.containsKey(name)) {
                obj.addProp(name, propval.traitID)
            }
        }
    }

    fun unapplyFrom(obj: Obj, forDestroy: Boolean = false) {
        objects.remove(obj.id)
        if (forDestroy) return  // no need to remove orphan props, obj is being destroyed
        // TODO: check this more efficiently, this is expensive
        obj.props.forEach { (name, propval) ->
            if (!obj.inheritsTrait(propval.traitID)) obj.removeProp(name)
        }
    }

    // Props

    fun addProp(propName: String, value: Value) {
        forEachDescendantTrait { trait ->
            if (trait.props.containsKey(propName)) throw IllegalArgumentException("trait $trait already has prop $propName")
        }
        forEachDescendantTrait { trait ->
            trait.props[propName] = Propval(this.id)
        }
        props[propName]!!.v = value
        forEachDescendantObj { obj ->
            if (!obj.hasProp(propName)) obj.addProp(propName, this.id)
        }
    }

    fun removeProp(propName: String) {
        if (!props.containsKey(propName)) throw IllegalArgumentException("trait has no property $propName")
        if (getPropOwner(propName) != id) throw IllegalArgumentException("prop $propName is not owned by trait")
        forEachDescendantTrait { trait ->
            trait.props.remove(propName)
        }
        forEachDescendantObj { obj ->
            obj.removeProp(propName)
        }
    }

    open fun getProp(propName: String): Value? = when (propName) {
        "objects" -> VList.make(objects.mapNotNull { it.obj()?.vThis })
        "parents" -> VList.make(parents.mapNotNull { it.trait()?.vTrait })
        "children" -> VList.make(children.mapNotNull { it.trait()?.vTrait })
        "commands" -> VList.make(commands.map { VList.make(listOf(VString(it.spec), VString(it.verb))) })
        "verbs" -> VList.make(verbs.keys.map { VString(it) })
        else -> props[propName]?.get(propName)
    }

    fun setProp(propName: String, value: Value): Boolean {
        props[propName]?.also { oldval ->
            if (oldval.traitID == this.id || oldval.v != null) {
                props[propName] = Propval(oldval.traitID, value)
            } else {
                val oldTraitID = oldval.traitID
                props[propName] = Propval(oldTraitID, value)
                // Prop is now uncleared; set new traitID on all inheriting traits + objs
                forEachDescendantTrait { trait ->
                    if (trait != this) {
                        if (trait.props[propName]!!.traitID == oldTraitID) {
                            val oldValue = trait.props[propName]!!.v
                            trait.props[propName] = Propval(this.id, oldValue)
                        }
                    }
                }
                forEachDescendantObj { obj ->
                    if (obj.props[propName]!!.traitID == oldTraitID) {
                        val oldValue = obj.props[propName]!!.v
                        obj.props[propName] = Propval(this.id, oldValue)
                    }
                }
            }
            return true
        }
        return false
    }

    fun clearProp(propName: String) {
        if (!props.containsKey(propName)) throw IllegalArgumentException("trait has no property $propName")
        val newParentID = getPropOwner(propName)!!
        if (newParentID == id) throw IllegalArgumentException("prop $propName is owned by trait, and can't be cleared on it")
        if (props[propName]!!.v == null) return  // already clear
        props[propName]!!.v = null
        forEachDescendantTrait { trait ->
            val oldPropval = trait.props[propName]!!
            if (oldPropval.traitID == id) {
                trait.props[propName] = Propval(newParentID, oldPropval.v)
            }
        }
        forEachDescendantObj { obj ->
            val oldPropval = obj.props[propName]!!
            if (oldPropval.traitID == id) {
                obj.props[propName] = Propval(newParentID, oldPropval.v)
            }
        }
    }

    // Find trait in our ancestry which defines a prop.
    private fun getPropOwner(propName: String): ID? {
        if (!props.containsKey(propName)) return null
        parents.forEach { parent ->
            val fromParent = parent.trait()!!.getPropOwner(propName)
            if (fromParent != null) return fromParent
        }
        return id
    }

    // Verbs

    fun getVerb(name: String): Verb? {
        if (verbs.containsKey(name)) return verbs[name]
        return getPassVerb(name)
    }

    inline fun getPassVerb(name: String): Verb? {
        for (p in parents) {
            p.trait()!!.getVerb(name)?.also { return it }
        }
        return null
    }

    fun programVerb(verbName: String, source: String) {
        verbs[verbName]?.also {
            it.program(source)
        } ?: run {
            verbs[verbName] = Verb(verbName, id).apply { program(source) }
        }
    }

    fun removeVerb(verbName: String) {
        verbs.remove(verbName)
    }

    open fun callStaticVerb(c: Context, verbName: String, args: List<Value>): Value? = null

    // Commands

    fun setCommand(command: Command) {
        commands.removeIf { it.verb == command.verb }
        commands.add(command)
    }

    fun removeCommand(spec: String) {
        commands.removeIf { it.spec == spec }
    }

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

        parents.mapNotNull { it.trait() }.forEach { parent ->
            parent.matchCommand(obj, cmdstr, argstr, dobjstr, dobj, prep, iobjstr, iobj)?.also { return it }
        }

        return null
    }

}
