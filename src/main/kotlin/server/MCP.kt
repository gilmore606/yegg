package com.dlfsystems.server

import com.dlfsystems.vm.Context
import com.dlfsystems.util.systemEpoch
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Executable
import kotlinx.coroutines.*
import java.util.*

// The task scheduler.
// Runs tasks at scheduled epoch times in a single thread.

object MCP {

    private const val WAIT_FOR_TASKS_MS = 10L
    private val taskMap = TreeMap<TaskID, Task>()
    private var job: Job? = null

    // Schedule an executable to run some seconds in the future.
    fun schedule(
        c: Context,
        exe: Executable,
        args: List<Value>,
        secondsInFuture: Int = 0
    ) {
        val task = Task(systemEpoch() + secondsInFuture, c, exe, args)
        taskMap[task.id] = task
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {
        if (job?.isActive == true) throw IllegalStateException("Already started")
        job = GlobalScope.launch {
            runTasks()
        }
    }

    fun stop() {
        job?.cancel()
    }

    private suspend fun runTasks() {
        while (true) {
            getNextTask()?.also { task ->
                taskMap.remove(task.id)
                runTask(task)
            } ?: run {
                delay(WAIT_FOR_TASKS_MS)
            }
        }
    }

    private fun getNextTask(): Task? {
        if (taskMap.isEmpty()) return null
        val nextTask = taskMap[taskMap.firstKey()]
        if (nextTask!!.atEpoch > systemEpoch()) return null
        return nextTask
    }

    private fun runTask(task: Task) {
        try {
            task.exe.execute(task.c, task.args)
        } catch (e: SuspendException) {
            // TODO: how tf is this gonna work
            // TODO: the resumed task will assume it can return values back through the stack, but those calls no longer exist
            // TODO: reevaluate calling/returning through callstack
            // schedule(task.apply { atEpoch = systemEpoch() + e.seconds })
        } catch (e: Exception) {
            task.c.connection?.sendText(e.toString())
            task.c.connection?.sendText(task.c.stackDump())
        }
    }
}


// Throw me to suspend the current task
class SuspendException(val seconds: Int): Exception()
