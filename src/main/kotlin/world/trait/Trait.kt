package com.dlfsystems.world.trait

import com.dlfsystems.Yegg
import com.dlfsystems.app.Log
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.VList
import com.dlfsystems.value.VObj
import com.dlfsystems.value.VTrait
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.world.Obj
import kotlinx.serialization.Serializable
import ulid.ULID

// A collection of verbs and props, which can apply to an Obj.

@Serializable
open class Trait(val name: String) {

    val id: ULID = ULID.nextULID()
    private val vTrait = VTrait(id)

    val traits: MutableList<ULID> = mutableListOf()

    val verbs: MutableMap<String, Verb> = mutableMapOf()
    open val props: MutableMap<String, Value> = mutableMapOf()

    val objects: MutableSet<ULID> = mutableSetOf()

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

    fun hasTrait(trait: ULID): Boolean = (trait in traits) || (traits.first { Yegg.world.getTrait(it)?.hasTrait(trait) ?: false } != null)

    fun programVerb(verbName: String, cOut: Compiler.Result) {
        verbs[verbName]?.also {
            it.program(cOut)
        } ?: run {
            verbs[verbName] = Verb(verbName).apply { program(cOut) }
        }
    }

    open fun getProp(c: Context, propName: String): Value? {
        return when (propName) {
            "objects" -> return VList(objects.map { VObj(it) }.toMutableList())
            else -> props.getOrDefault(propName, null)
        }
    }

    open fun setProp(c: Context, propName: String, value: Value): Boolean {
        props[propName] = value
        return true
    }

    open fun callVerb(c: Context, verbName: String, args: List<Value>): Value? {
        verbs[verbName]?.also {
            Log.i("static verb call: \$$name.$verbName($args)")
            return it.call(c, Yegg.vNullObj, vTrait, args)
        }
        return null
    }

}
