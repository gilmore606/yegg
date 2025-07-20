package com.dlfsystems.yegg.server.mcp

import com.dlfsystems.yegg.server.Connection
import com.dlfsystems.yegg.server.Log
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.util.systemEpoch
import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.vm.VM
import com.dlfsystems.yegg.vm.VMException
import kotlinx.coroutines.*
import java.util.*

// The task scheduler.
// Runs tasks at scheduled epoch times in a single thread.

object MCP {

    private const val WAIT_FOR_TASKS_MS = 1L

    private val timeMap = TreeMap<TimeID, Task>()
    private val taskMap = HashMap<Task.ID, Task>()

    private var job: Job? = null

    // Schedule a task to run some seconds in the future.
    fun schedule(
        task: Task,
        secondsInFuture: Int = 0
    ) {
        task.setTime(secondsInFuture)
        timeMap[task.timeID] = task
        taskMap[task.id] = task
    }

    // Cancel a scheduled task.
    fun cancel(taskID: Task.ID) {
        taskMap[taskID]?.also { task ->
            taskMap.remove(task.id)
            timeMap.remove(task.timeID)
        }
    }

    // Move a scheduled task to immediate execution.
    fun resume(taskID: Task.ID) { resumeWithResult(taskID, null) }

    // Resume with a result pushed onto the stack.
    fun resumeWithResult(taskID: Task.ID, result: Value?) {
        taskMap[taskID]?.also { task ->
            task.resumeResult = result
            timeMap.remove(task.timeID)
            task.setTime(0)
            timeMap[task.timeID] = task
        } ?: throw IllegalArgumentException("Task $taskID does not exist")
    }

    fun isValidTask(taskID: Task.ID) = taskMap.containsKey(taskID)

    fun taskList() = taskMap.values.toList()

    fun start() {
        if (job?.isActive == true) throw IllegalStateException("Already started")
        job = Yegg.launch {
            runTasks()
        }
        Log.i(TAG, "Started.")
    }

    fun stop() {
        job?.cancel()
        Log.i(TAG, "Stopped.")
    }

    private suspend fun runTasks() {
        while (true) {
            var task = getNextTask()
            while (task != null) {
                Log.d(TAG, "Executing ${task.id} $task")
                timeMap.remove(task.timeID)
                taskMap.remove(task.id)
                val result = task.execute()
                when (result) {
                    is Task.Result.Suspend -> {
                        task.setTime(result.seconds)
                        timeMap[task.timeID] = task
                        taskMap[task.id] = task
                    }
                    is Task.Result.Failed -> {
                        task.connection?.also {
                            outputError(it, result.e, result.stack)
                        } ?: run {
                            Log.i(TAG, "Headless task exception:\n$ {result.e}\n$ {result.stack}")
                        }
                    }
                    is Task.Result.Finished -> { }
                }
                task = getNextTask()
            }

            Yegg.processNextInputs()
            Log.flush()
            delay(WAIT_FOR_TASKS_MS)
        }
    }

    private fun getNextTask(): Task? {
        if (timeMap.isEmpty()) return null
        timeMap[timeMap.firstKey()]?.also { nextTask ->
            if (nextTask.atEpoch > systemEpoch()) return null
            return nextTask
        }
        return null
    }

    private fun outputError(
        connection: Connection,
        e: VMException,
        stack: List<VM>
    ) {
        connection.sendText(e.toString())
        stack.first().exe.getSourceLine(e.lineNum)?.also { line ->
            connection.sendText(line)
        }
        connection.sendText(stack.joinToString("\n", "", "\n"))
    }

    private const val TAG = "MCP"

}
