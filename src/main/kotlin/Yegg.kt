package com.dlfsystems

import com.dlfsystems.world.World
import com.dlfsystems.compiler.Compiler

object Yegg {

    lateinit var world: World

    fun start() {
        // TODO: load from file
        world = World().apply {
            addTrait("sys")
            addTrait("user")
        }
    }

    fun programVerb(traitName: String, name: String, code: String): String = world.programVerb(traitName, name, code)

    // Receive text from websocket.  Just a basic REPL for now.
    // TODO: users, auth, *waves hands*
    fun receiveText(text: String): String {
        return Compiler.eval(text)
    }
}
