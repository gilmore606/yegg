package com.dlfsystems.server.mcp

import com.dlfsystems.app.Log
import com.dlfsystems.server.Yegg
import com.dlfsystems.util.systemEpoch
import kotlinx.coroutines.*
import java.util.*


// The task scheduler.
// Runs tasks at scheduled epoch times in a single thread.

object MCP {

    private const val WAIT_FOR_TASKS_MS = 10L

    private val timeMap = TreeMap<TimeID, Task.ID>()
    private val taskMap = HashMap<Task.ID, Task>()

    private var job: Job? = null

    // Schedule a task to run some seconds in the future.
    fun schedule(
        task: Task,
        secondsInFuture: Int = 0
    ) {
        task.setTime(secondsInFuture)
        timeMap[task.timeID] = task.id
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
    fun resume(taskID: Task.ID) {
        taskMap[taskID]?.also { task ->
            timeMap.remove(task.timeID)
            task.setTime(0)
            timeMap[task.timeID] = task.id
        } ?: throw IllegalArgumentException("Task $taskID does not exist")
    }

    // Is the given taskID a valid scheduled task?
    fun isValidTask(taskID: Task.ID) = taskMap.containsKey(taskID)

    fun taskList() = taskMap.values.toList()

    // Start processing all queued tasks.
    fun start() {
        if (job?.isActive == true) throw IllegalStateException("Already started")
        job = Yegg.launch {
            runTasks()
        }
    }

    // Stop processing queued tasks.
    fun stop() {
        job?.cancel()
    }

    private suspend fun runTasks() {
        while (true) {
            getNextTask()?.also { task ->
                Log.d("Executing ${task.id} $task")
                timeMap.remove(task.timeID)
                taskMap.remove(task.id)
                val result = task.execute()
                if (result is Task.Result.Suspend) {
                    task.setTime(result.seconds)
                    timeMap[task.timeID] = task.id
                    taskMap[task.id] = task
                }
            } ?: run {
                delay(WAIT_FOR_TASKS_MS)
            }
        }
    }

    private fun getNextTask(): Task? {
        if (timeMap.isEmpty()) return null
        taskMap[timeMap[timeMap.firstKey()]]?.also { nextTask ->
            if (nextTask.atEpoch > systemEpoch()) return null
            return nextTask
        }
        return null
    }

}
