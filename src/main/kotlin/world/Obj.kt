package com.dlfsystems.world

import com.dlfsystems.value.VObj
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import java.util.*

// An instance in the world.

class Obj {

    val id: UUID = UUID.randomUUID()
    private val vThis = VObj(id)

    val traits: MutableList<UUID> = mutableListOf()

}
