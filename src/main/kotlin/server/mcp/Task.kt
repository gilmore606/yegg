package com.dlfsystems.yegg.server.mcp

import com.dlfsystems.yegg.server.Connection
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.util.NanoID
import com.dlfsystems.yegg.util.systemEpoch
import com.dlfsystems.yegg.value.VObj
import com.dlfsystems.yegg.value.VTask
import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.vm.*
import com.dlfsystems.yegg.vm.VMException.Type.*
import kotlinx.serialization.Serializable

class Task(
    override val connection: Connection? = null,
    override val vThis: VObj = Yegg.vNullObj,
    override var vUser: VObj = connection?.user?.vThis ?: Yegg.vNullObj,
) : Context {

    @Serializable @JvmInline
    value class ID(val id: String) { override fun toString() = id }
    val id = ID(NanoID.newID())
    val vID = VTask(id)
    override val taskID
        get() = id
    // TimeID includes the schedule time, and changes with every rescheduling.
    var timeID = TimeID(0L)
    var atEpoch = 0

    // Result to push on stack before executing.  Set to return a result when resuming a suspend-for-result (i.e. readline).
    var resumeResult: Value? = null

    override var ticksLeft: Int = Yegg.world.getSysInt("tickLimit")
    override var callsLeft: Int = Yegg.world.getSysInt("callLimit")

    val stack = ArrayDeque<VM>()


    fun fail(type: VMException.Type, m: String) { throw VMException(type, m) }

    override fun toString() = stack.first().toString()

    fun setTime(secondsInFuture: Int) {
        atEpoch = if (secondsInFuture == Int.MAX_VALUE) Int.MAX_VALUE else systemEpoch() + secondsInFuture
        timeID = TimeID(atEpoch * 1000L)
    }


    sealed interface Result {
        data object Finished: Result
        @JvmInline value class Suspend(val seconds: Int): Result
    }

    // Execute the top stack frame.
    // On a Suspend, return Suspend upward to MCP.
    // On a Call, push a new stack frame.
    // On a Return, pop a stack frame, and save the return value to pass to the next iteration (the previous frame).
    // Continue until the stack is empty.

    fun execute(): Result {
        var vReturn: Value? = resumeResult
        resumeResult = null
        try {
            while (stack.isNotEmpty()) {
                stack.first().execute(vReturn).also { result ->
                    vReturn = null
                    when (result) {
                        is VM.Result.Return -> {
                            vReturn = result.v
                            callsLeft++
                            pop()
                        }
                        is VM.Result.Call -> {
                            if (--callsLeft < 0) fail(E_MAXREC, "too many nested verb calls")
                            push(result.vThis, result.exe, result.args)
                        }
                        is VM.Result.Suspend -> {
                            return Result.Suspend(result.seconds)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            connection?.sendText(e.toString())
            connection?.sendText(stackDump())
        }
        return Result.Finished
    }

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

    companion object {
        fun make(
            exe: Executable,
            args: List<Value> = listOf(),
            connection: Connection? = null,
            vThis: VObj = Yegg.vNullObj,
            vUser: VObj = Yegg.vNullObj,
        ) = Task(connection, vThis, vUser).apply {
                push(vThis, exe, args)
            }
    }

}
