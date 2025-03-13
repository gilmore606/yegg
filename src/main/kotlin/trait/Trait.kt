package com.dlfsystems.trait

import com.dlfsystems.vm.VMWord
import java.util.*
import kotlin.collections.ArrayList

// A collection of funcs and props, which can apply to Things.

class Trait(val name: String) {

    val id: UUID = UUID.randomUUID()

    val traits: MutableList<UUID> = mutableListOf()
    val funcs: MutableList<Func> = mutableListOf()
    val props: MutableMap<String, Prop> = mutableMapOf()

    fun programFunc(name: String, code: List<VMWord>) {
        funcs.firstOrNull { it.name == name }?.also {
            it.program(code)
        } ?: {
            funcs.add(Func(name).apply { program(code) })
        }
    }


}
