package com.dlfsystems.yegg.server.mcp

import com.dlfsystems.yegg.server.Connection
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.util.NanoID
import com.dlfsystems.yegg.util.systemEpoch
import com.dlfsystems.yegg.value.VObj
import com.dlfsystems.yegg.value.VTask
import com.dlfsystems.yegg.value.VVoid
import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.vm.*
import com.dlfsystems.yegg.vm.VMException.Type.*
import kotlinx.serialization.Serializable

class Task(
    override val connection: Connection? = null,
    override val vThis: VObj = Yegg.vNullObj,
    override var vPlayer: VObj = connection?.player?.vThis ?: Yegg.vNullObj,
    val onResult: ((Result) -> Unit)? = null,
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

    // Stack of VMs awaiting return values.
    val stack = ArrayDeque<VM>()


    fun fail(type: VMException.Type, m: String) { throw VMException(type, m) }

    override fun toString() = stack.first().toString()

    fun setTime(secondsInFuture: Int) {
        atEpoch = if (secondsInFuture == Int.MAX_VALUE) Int.MAX_VALUE else systemEpoch() + secondsInFuture
        timeID = TimeID(atEpoch * 1000L)
    }


    sealed interface Result {
        data class Finished(val v: Value): Result
        data class Suspend(val seconds: Int): Result
        data class Failed(val e: VMException, val stack: List<VM>): Result
    }

    // Execute the top stack frame.
    // On a Suspend, return Suspend upward to MCP.
    // On a Call, push a new stack frame.
    // On a Return, pop a stack frame, and save the return value to pass to the next iteration (the previous frame).
    // Continue until the stack is empty.
    fun execute(toDepth: Int = 0): Result {
        var vReturn: Value? = resumeResult
        resumeResult = null
        while (true) {
            try {
                while (stack.size > toDepth) {
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
                val result = Result.Finished(vReturn ?: VVoid)
                if (stack.isEmpty()) onResult?.invoke(result)
                return result
            } catch (e: Exception) {
                val vm = stack.first()
                val err = (e as? VMException ?: VMException(
                    E_SYS, "${e.message}\n${e.stackTraceToString()}"
                )).withPos(vm.pos)
                var caught = false
                val errorStack = buildList { addAll(stack) }
                while (stack.isNotEmpty() && !caught) {
                    if (peek().catchError(err)) caught = true
                    else pop()
                }
                if (!caught) return Result.Failed(err, errorStack)
            }
        }
    }

    // Execute an exe immediately for a return value.  The task cannot suspend.
    // Used by system functions to call verb code in an existing Task.
    override fun executeForResult(exe: Executable, args: List<Value>): Value {
        push(vThis, exe, args)
        execute(toDepth = stack.size - 1).also { result ->
            when (result) {
                is Result.Suspend -> fail(E_LIMIT, "cannot suspend in verb called by system")
                is Result.Failed -> throw result.e
                is Result.Finished -> return result.v
            }
        }
        return VVoid
    }

    private fun push(vThis: VObj?, exe: Executable, args: List<Value>) {
        exe.jitCompile()
        stack.addFirst(VM(this, vThis, exe, args))
    }

    private fun peek(): VM = stack.first()
    private fun pop(): VM = stack.removeFirst()


    companion object {
        fun make(
            exe: Executable,
            args: List<Value> = listOf(),
            connection: Connection? = null,
            vThis: VObj = Yegg.vNullObj,
            vPlayer: VObj = Yegg.vNullObj,
            onResult: ((Result) -> Unit)? = null,
        ) = Task(connection, vThis, vPlayer, onResult).apply {
                push(vThis, exe, args)
            }
    }

}
