package com.dlfsystems.trait

import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMWord
import java.util.*

// A collection of funcs and props, which can apply to Things.

class Trait(val name: String) {

    val id: UUID = UUID.randomUUID()

    val funcs: MutableMap<String, Func> = mutableMapOf()
    val props: MutableMap<String, Prop> = mutableMapOf()

    fun programFunc(name: String, code: List<VMWord>) {
        funcs[name]?.also {
            it.program(code)
        } ?: {
            funcs[name] = Func(name).apply { program(code) }
        }
    }

    fun callFunc(c: Context, name: String): Value? = funcs[name]?.execute(c)

}
