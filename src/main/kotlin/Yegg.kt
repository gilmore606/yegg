package com.dlfsystems

import com.dlfsystems.world.World

object Yegg {

    lateinit var world: World

    fun start() {
        // TODO: load from file
        world = World().apply {
            addTrait("sys")
            addTrait("user")
        }
    }

    fun programFunc(traitName: String, funcName: String, code: String): String = world.programFunc(traitName, funcName, code)
}
