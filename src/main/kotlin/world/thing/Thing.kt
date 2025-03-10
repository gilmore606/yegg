package com.dlfsystems.world.thing

import com.dlfsystems.trait.Trait
import java.util.*

// An instance in the world.

class Thing {

    val id: UUID = UUID.randomUUID()

    val traits: Map<String, Trait> = mapOf()
    val props: Map<String, Prop> = mapOf()

}
