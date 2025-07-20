package com.dlfsystems.yegg.server.mcp

import com.dlfsystems.yegg.server.Connection
import com.dlfsystems.yegg.server.Log
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.util.stripAnsi
import com.dlfsystems.yegg.util.systemEpoch
import com.dlfsystems.yegg.util.xColor
import com.dlfsystems.yegg.util.xUnderline
import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.vm.VM
import com.dlfsystems.yegg.vm.VMException
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.max
import kotlin.math.min

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
        val em = xColor(203, e.type.toString()) +
                xColor(210, ": ${e.m}") +
                xColor(243, " in ") +
                stack.first().toString() +
                xColor(243, " [${stack.first().lineNum}:${stack.first().charNum}]")
        val sm = stack.first().exe.getSourceLine(e.lineNum)?.let {
            val c = max(0, min(stack.first().charNum - 1, it.lastIndex))
            if (c == 0) {
                xColor(225, 52, xUnderline(it.substring(0, 0))) + xColor(195, 236, it.substring(1))
            } else if (c == it.lastIndex) {
                xColor(195, 236, it.substring(0, c)) + xColor(225, 52, xUnderline(it.substring(c, c+1)))
            } else {
                xColor(195, 236, it.substring(0, c)) + xColor(225, 52, xUnderline(it.substring(c, c+1))) +
                    xColor(195, 236, it.substring(c+1))
            }
        }
        val lines = buildList { stack.forEach { vm ->
            add(
                " (${vm.vThis}) " +
                vm.exe.toString() +
                "(" + vm.args.joinToString(",") + ")"
            )
        } }

        var maxLen = 0
        lines.forEach { maxLen = max(maxLen, it.stripAnsi().length) }

        connection.sendText(em)
        sm?.also { connection.sendText(it) }
        lines.forEachIndexed { n, line ->
            connection.sendText(line.padEnd(maxLen + 2) + xColor(243, " [" + stack[n].lineNum + "]"))
        }
    }


    private const val TAG = "MCP"

}
