package com.dlfsystems.trait

import java.util.*
import kotlin.collections.ArrayList

// A collection of funcs and props, which can apply to Things.

class Trait {

    val id: UUID = UUID.randomUUID()

    val traits: MutableList<Trait> = ArrayList()
    val funcs: MutableList<Func> = ArrayList()

}
