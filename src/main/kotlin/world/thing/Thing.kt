package com.dlfsystems.world.thing

import com.dlfsystems.value.Value
import java.util.*

// An instance in the world.

class Thing {

    val id: UUID = UUID.randomUUID()

    val traits: MutableList<UUID> = mutableListOf()
    val props: Map<String, Value> = mapOf()

}
