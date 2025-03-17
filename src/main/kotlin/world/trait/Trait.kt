package com.dlfsystems.world.trait

import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMWord
import java.util.*

// A collection of funcs and props, which can apply to an Obj.

open class Trait(val name: String) {

    val id: UUID = UUID.randomUUID()

    val funcs: MutableMap<String, Func> = mutableMapOf()
    open val props: MutableMap<String, Value> = mutableMapOf()

    fun programFunc(name: String, code: List<VMWord>) {
        funcs[name]?.also {
            it.program(code)
        } ?: {
            funcs[name] = Func(name).apply { program(code) }
        }
    }

    open fun callFunc(c: Context, name: String): Value? = funcs[name]?.execute(c)

    open fun getProp(c: Context, name: String): Value? = props.getOrDefault(name, null)
    open fun setProp(c: Context, name: String, value: Value): Boolean {
        props[name] = value
        return true
    }
    open fun setPropIndex(c: Context, name: String, index: Value, value: Value): Boolean {
        return props[name]!!.setIndex(c, index, value)
    }

}
