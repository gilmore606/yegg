package com.dlfsystems.vm

import com.dlfsystems.world.World
import java.util.Stack

// Variables from the world which a VM uses to execute a func.
// A persistent VM will own a context whose values are updated from outside it.

class Context {

    var world: World? = null

    var vThis: Value? = objectValue(null)
    var vPlayer: Value? = objectValue(null)

    val callStack = VMCallstack()

}
