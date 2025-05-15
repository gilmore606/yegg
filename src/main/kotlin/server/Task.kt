package com.dlfsystems.server

import com.dlfsystems.util.TaskIDGenerator
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.vm.Executable
import kotlinx.serialization.Serializable
import java.lang.Comparable


@Serializable
data class TaskID(val id: String): Comparable<TaskID> {
    override fun toString() = id
    override fun compareTo(other: TaskID) = id.compareTo(other.id)
}

data class Task(
    var atEpoch: Int,
    val c: Context,
    val exe: Executable,
    val args: List<Value>,
) {
    val id = TaskID(TaskIDGenerator.generate(atEpoch * 1000L))
}
