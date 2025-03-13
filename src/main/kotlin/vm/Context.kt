package com.dlfsystems.vm

import com.dlfsystems.world.World
import com.dlfsystems.value.*

// Variables from the world which a VM uses to execute a func.
// A persistent VM will own a context whose values are updated from outside it.

class Context(
    val world: World? = null
) {

    var vThis: VThing = VThing(null)
    var vPlayer: VThing = VThing(null)

    val callStack = VMCallstack()

}
