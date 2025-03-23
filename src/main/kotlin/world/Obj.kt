package com.dlfsystems.world

import com.dlfsystems.value.VObj
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

// An instance in the world.

@Serializable
class Obj {

    val id: Uuid = Uuid.random()
    private val vThis = VObj(id)

    val traits: MutableList<Uuid> = mutableListOf()

}
