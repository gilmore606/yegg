package com.dlfsystems.vm

import com.dlfsystems.world.World
import com.dlfsystems.value.*
import java.util.UUID

// Variables from the world which a VM uses to execute a func.
// A persistent VM will own a context whose values are updated from outside it.

class Context(
    val world: World = World()
) {
    class Call(
        val vThis: Value,
        val verb: String,
        val args: List<Value>,
    )

    var vThis: VObj = VObj(null)
    var vUser: VObj = VObj(null)

    var ticksLeft: Int = (world.getSysValue(this, "tickLimit") as VInt).v
    val callStack = ArrayDeque<Call>()

    // Push or pop the callstack.
    fun push(vThis: Value, verb: String, args: List<Value>) = callStack.addFirst(Call(vThis, verb, args))
    fun pop(): Call = callStack.removeFirst()

    fun getTrait(name: String) = world.getTrait(name)
    fun getTrait(id: UUID) = world.getTrait(id)

}
