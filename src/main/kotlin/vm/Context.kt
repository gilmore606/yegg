package com.dlfsystems.vm

import com.dlfsystems.world.World
import com.dlfsystems.value.*
import java.util.UUID

// Variables from the world which a VM uses to execute a func.
// A persistent VM will own a context whose values are updated from outside it.

class Context(
    val world: World? = null
) {

    var vThis: VObj = VObj(null)
    var vPlayer: VObj = VObj(null)

    val callStack = VMCallstack()

    fun getTrait(name: String) = world?.getTrait(name)
    fun getTrait(id: UUID) = world?.getTrait(id)
}
