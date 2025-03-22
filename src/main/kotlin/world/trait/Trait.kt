package com.dlfsystems.world.trait

import com.dlfsystems.app.Log
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMWord
import java.util.*

// A collection of verbs and props, which can apply to an Obj.

open class Trait(val name: String) {

    val id: UUID = UUID.randomUUID()

    val verbs: MutableMap<String, Verb> = mutableMapOf()
    open val props: MutableMap<String, Value> = mutableMapOf()

    fun programVerb(name: String, code: List<VMWord>, variableIDs: Map<String, Int>) {
        verbs[name]?.also {
            it.program(code, variableIDs)
        } ?: run {
            verbs[name] = Verb(name).apply { program(code, variableIDs) }
        }
    }

    open fun getProp(c: Context, propName: String): Value? = props.getOrDefault(propName, null)
    open fun setProp(c: Context, propName: String, value: Value): Boolean {
        props[propName] = value
        return true
    }

    open fun callVerb(c: Context, verbName: String, args: List<Value>): Value? {
        verbs[verbName]?.also { verb ->
            Log.i("Call \$$name.$verbName($args)")
            return verb.call(c, args)
        }
        return null
    }

}
