package com.dlfsystems.yegg.vm

import com.dlfsystems.yegg.server.mcp.Task
import com.dlfsystems.yegg.value.VObj
import com.dlfsystems.yegg.value.Value


interface Context {

    val connection: com.dlfsystems.yegg.server.Connection?
    val taskID: Task.ID?

    val vThis: VObj
    var vUser: VObj

    var ticksLeft: Int
    var callsLeft: Int

    fun executeLambda(exe: Executable, args: List<Value>): Value

}
