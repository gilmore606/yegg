package com.dlfsystems.server.mcp

import com.dlfsystems.server.parser.Connection
import com.dlfsystems.server.Yegg
import com.dlfsystems.util.NanoID
import com.dlfsystems.util.systemEpoch
import com.dlfsystems.value.VObj
import com.dlfsystems.value.VTask
import com.dlfsystems.value.Value
import com.dlfsystems.vm.*
import com.dlfsystems.vm.VMException.Type.*
import kotlinx.serialization.Serializable

class Task(val c: Context) {

    @Serializable
    data class ID(val id: String) { override fun toString() = id }
    val id = ID(NanoID.newID())
    val vID = VTask(id)

    // TimeID includes the schedule time, and changes with every rescheduling.
    var timeID = TimeID(0L)
    var atEpoch = 0

    fun fail(type: VMException.Type, m: String) { throw VMException(type, m) }

    override fun toString() = c.stack.first().toString()

    fun setTime(secondsInFuture: Int) {
        atEpoch = systemEpoch() + secondsInFuture
        timeID = TimeID(atEpoch * 1000L)
    }


    sealed class Result {
        data object Finished: Result()
        data class Suspend(val seconds: Int): Result()
    }

    // Execute the top stack frame.
    // On a Suspend, return Suspend upward to MCP.
    // On a Call, push a new stack frame.
    // On a Return, pop a stack frame, and save the return value to pass to the next iteration (the previous frame).
    // Continue until the stack is empty.

    fun execute(): Result {
        try {
            var vReturn: Value? = null
            while (c.stack.isNotEmpty()) {
                val vmResult = c.stack.first().execute(vReturn)
                vReturn = null
                when (vmResult) {
                    is VM.Result.Return -> {
                        vReturn = vmResult.v
                        c.callsLeft++
                        c.pop()
                    }
                    is VM.Result.Call -> {
                        if (--c.callsLeft < 0) fail(E_MAXREC, "too many nested verb calls")
                        c.push(vmResult.vThis, vmResult.exe, vmResult.args)
                    }
                    is VM.Result.Suspend -> {
                        return Result.Suspend(vmResult.seconds)
                    }
                }
            }
        } catch (e: Exception) {
            c.connection?.sendText(e.toString())
            c.connection?.sendText(c.stackDump())
        }
        return Result.Finished
    }

    companion object {
        fun make(
            exe: Executable,
            args: List<Value> = listOf(),
            connection: Connection? = null,
            vThis: VObj = Yegg.vNullObj,
            vUser: VObj = Yegg.vNullObj,
        ) = Task(
            Context(connection, vThis, vUser).apply {
                push(vThis, exe, args)
            }
        )
    }

}
