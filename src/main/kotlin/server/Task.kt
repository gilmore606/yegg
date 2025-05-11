package com.dlfsystems.server

import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.world.trait.TraitID


data class TaskID(val id: String) { override fun toString() = id }

data class Task(
    var atSeconds: Int,
    val c: Context,
    val trait: TraitID,
    val verb: String,
    val args: List<Value>,
) {
    val id = TaskID(Yegg.newID())
}
