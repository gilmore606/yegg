package com.dlfsystems.vm

import com.dlfsystems.world.World

// Variables from the world which a VM uses to execute a func.
// A persistent VM will own a context whose values are updated from outside it.

class Context {

    var world: World? = null

    var vThis: Value? = objectV(null)
    var vPlayer: Value? = objectV(null)

    val callStack = VMCallstack()

}
