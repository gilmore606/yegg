package com.dlfsystems.server

import com.dlfsystems.vm.Context
import com.dlfsystems.util.systemEpoch
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Executable
import kotlinx.coroutines.delay
import java.util.*

// The task scheduler.
// Runs tasks at scheduled epoch times in a single thread.

object MCP {

    private const val WAIT_FOR_TASKS_MS = 100L

    private val taskMap = mutableMapOf<TaskID, Task>()
    private val timeTree = TreeMap<Int, MutableList<TaskID>>()


    fun schedule(
        c: Context,
        exe: Executable,
        args: List<Value>,
        secondsInFuture: Int = 0
    ) {
        schedule(Task(systemEpoch() + secondsInFuture, c, exe, args))
    }

    private fun schedule(task: Task) {
        taskMap[task.id] = task
        timeTree[task.atSeconds]?.add(task.id) ?: run {
            timeTree.put(task.atSeconds, mutableListOf(task.id))
        }
    }

    suspend fun runTasks() {
        while (true) {
            getNextTask()?.also { task ->
                taskMap.remove(task.id)
                timeTree[task.atSeconds]!!.apply {
                    remove(task.id)
                    if (isEmpty()) timeTree.remove(task.atSeconds)
                }
                runTask(task)
            } ?: run {
                delay(WAIT_FOR_TASKS_MS)
            }
        }
    }

    private fun getNextTask(): Task? {
        if (timeTree.isEmpty()) return null
        // Find lowest time we have tasks for
        val exeTime = timeTree.firstKey()
        // If that's in the future, abort
        if (exeTime > systemEpoch()) return null
        // Return the first task in the current second
        return taskMap[timeTree[exeTime]!!.first()]
    }

    private fun runTask(task: Task) {
        try {
            task.exe.execute(task.c, task.args)
        } catch (e: SuspendException) {
            // TODO: how tf is this gonna work
            // TODO: the resumed task will assume it can return values back through the stack, but those calls no longer exist
            // TODO: reevaluate calling/returning through callstack
            schedule(task.apply { atSeconds = systemEpoch() + e.seconds })
        } catch (e: Exception) {
            task.c.connection?.sendText(e.toString())
            task.c.connection?.sendText(task.c.stackDump())
        }
    }
}


// Throw me to suspend the current task
class SuspendException(val seconds: Int): Exception()
