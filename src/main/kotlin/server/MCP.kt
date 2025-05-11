package com.dlfsystems.server

import com.dlfsystems.vm.Context
import com.dlfsystems.util.systemEpoch
import com.dlfsystems.value.VFun
import com.dlfsystems.value.Value
import com.dlfsystems.world.trait.TraitID
import kotlinx.coroutines.delay


object MCP {

    private val taskMap = mutableMapOf<TaskID, Task>()
    private val timeMap = mutableMapOf<Int, MutableList<TaskID>>()


    fun schedule(vFun: VFun, seconds: Int = 0) {

    }

    fun schedule(c: Context, trait: TraitID, verb: String, args: List<Value>, seconds: Int = 0) {
        schedule(Task(systemEpoch() + seconds, c, trait, verb, args))
    }

    fun schedule(task: Task) {
        taskMap[task.id] = task
        timeMap[task.atSeconds]?.add(task.id) ?: run {
            timeMap[task.atSeconds] = mutableListOf(task.id)
        }
    }

    suspend fun runTasks() {
        while (true) {
            getNextTask()?.also { task ->
                taskMap.remove(task.id)
                timeMap[task.atSeconds]!!.apply {
                    remove(task.id)
                    if (isEmpty()) timeMap.remove(task.atSeconds)
                }
                runTask(task)
            } ?: run {
                delay(100L)
            }
        }
    }

    private fun getNextTask(): Task? {
        if (timeMap.isEmpty()) return null
        // Find lowest time we have tasks for
        var exeTime = timeMap.keys.first()
        timeMap.keys.forEach { if (it < exeTime) exeTime = it }
        // If that's in the future, abort
        if (exeTime > systemEpoch()) return null
        // Return the first task in the current second
        return timeMap[exeTime]!!.let { taskMap[it.first()] }
    }

    private fun runTask(task: Task) {
        try {
            Yegg.world.getTrait(task.trait)!!.callVerb(task.c, task.verb, task.args)
        } catch (e: SuspendException) {
            // TODO: how tf is this gonna work
            schedule(task.apply { atSeconds = systemEpoch() + e.seconds })
        } catch (e: Exception) {
            task.c.connection?.sendText(e.toString())
            task.c.connection?.sendText(task.c.stackDump())
        }
    }
}
