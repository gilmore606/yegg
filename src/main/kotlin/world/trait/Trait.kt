package com.dlfsystems.world.trait

import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMWord
import java.util.*

// A collection of verbs and props, which can apply to an Obj.

open class Trait(val name: String) {

    val id: UUID = UUID.randomUUID()

    val verbs: MutableMap<String, Verb> = mutableMapOf()
    open val props: MutableMap<String, Value> = mutableMapOf()

    fun programVerb(name: String, code: List<VMWord>) {
        verbs[name]?.also {
            it.program(code)
        } ?: {
            verbs[name] = Verb(name).apply { program(code) }
        }
    }

    open fun getProp(c: Context, name: String): Value? = props.getOrDefault(name, null)
    open fun setProp(c: Context, name: String, value: Value): Boolean {
        props[name] = value
        return true
    }

    open fun callVerb(c: Context, name: String, args: List<Value>): Value? {
        verbs[name]?.also {
            // TODO execute verb w args
        }
        return null
    }

}
