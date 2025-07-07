package com.dlfsystems.yegg.vm

import com.dlfsystems.yegg.server.Connection
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.server.mcp.Task
import com.dlfsystems.yegg.value.*

// Properties of a single command invocation as it passes from verb to verb.

class Context(
    val connection: Connection? = null,
    val vThis: VObj = Yegg.vNullObj,
    var vUser: VObj = connection?.user?.vThis ?: Yegg.vNullObj,
) {
    var taskID: Task.ID? = null

    var ticksLeft: Int = Yegg.world.getSysInt("tickLimit")
    var callsLeft: Int = Yegg.world.getSysInt("callLimit")

    val stack = ArrayDeque<VM>()

    // Add a VM to the stack to run an exe.
    fun push(
        vThis: VObj,
        exe: Executable,
        args: List<Value>,
    ) {
        exe.jitCompile()
        stack.addFirst(
            VM(this, vThis, exe, args)
        )
    }

    fun pop(): VM {
        return stack.removeFirst()
    }

    fun stackDump() = stack.joinToString(prefix = "...", separator = "\n...", postfix = "\n")

}
