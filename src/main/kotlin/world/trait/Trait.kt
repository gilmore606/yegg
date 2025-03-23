package com.dlfsystems.world.trait

import com.dlfsystems.app.Log
import com.dlfsystems.value.VTrait
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMWord
import java.util.*

// A collection of verbs and props, which can apply to an Obj.

open class Trait(val name: String) {

    val id: UUID = UUID.randomUUID()
    private val vThis = VTrait(id)

    val verbs: MutableMap<String, Verb> = mutableMapOf()
    open val props: MutableMap<String, Value> = mutableMapOf()

    fun programVerb(verbName: String, code: List<VMWord>, variableIDs: Map<String, Int>) {
        verbs[verbName]?.also {
            it.program(code, variableIDs)
        } ?: run {
            verbs[verbName] = Verb(verbName).apply { program(code, variableIDs) }
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
            return it.call(c, vThis, args)
        }
        return null
    }

}
