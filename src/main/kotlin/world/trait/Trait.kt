package com.dlfsystems.world.trait

import com.dlfsystems.Yegg
import com.dlfsystems.app.Log
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.VTrait
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import kotlin.uuid.Uuid

// A collection of verbs and props, which can apply to an Obj.

open class Trait(val name: String) {

    val id: Uuid = Uuid.random()
    private val vTrait = VTrait(id)

    val verbs: MutableMap<String, Verb> = mutableMapOf()
    open val props: MutableMap<String, Value> = mutableMapOf()

    fun programVerb(verbName: String, cOut: Compiler.Result) {
        verbs[verbName]?.also {
            it.program(cOut)
        } ?: run {
            verbs[verbName] = Verb(verbName).apply { program(cOut) }
        }
    }

    open fun getProp(c: Context, propName: String): Value? = props.getOrDefault(propName, null)
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
