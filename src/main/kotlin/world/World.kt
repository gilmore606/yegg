package com.dlfsystems.world

import com.dlfsystems.world.thing.Thing
import java.util.UUID

class World {

    val things: MutableMap<UUID, Thing> = mutableMapOf()

}
