package com.dlfsystems

import com.dlfsystems.world.World
import com.dlfsystems.compiler.Compiler

object Yegg {

    // TODO: users, auth, *waves hands*
    class Connection() {

        var buffer = mutableListOf<String>()
        var programming: Pair<String, String>? = null

        // Receive text from websocket.  Just a basic REPL for now.
        fun receiveText(text: String): String {
            programming?.also { verb ->
                if (text == ".") {
                    val result = programFunc(verb.first, verb.second, buffer.joinToString("\n"))
                    buffer.clear()
                    programming = null
                    return result
                } else buffer.add(text)
            } ?: run {
                return if (text.startsWith("@")) {
                    parseMeta(text)
                } else {
                    Compiler.eval(text)
                }
            }
            return ""
        }

        // Respond to meta commands.
        fun parseMeta(text: String): String {
            if (text == "@") return ""
            val words = text.split(" ")
            return when (words[0].substring(1, words[0].length)) {
                "program" -> parseProgram(words)
                else -> "I don't understand that."
            }
        }

        fun parseProgram(words: List<String>): String {
            if (words.size < 2) return "@program what?"
            val terms = words[1].split(".")
            if (terms.size != 2) return "@program what?"
            programming = Pair(terms[0], terms[1])
            return "Enter verbcode.  Terminate with '.' on a line by itself."
        }
    }

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
