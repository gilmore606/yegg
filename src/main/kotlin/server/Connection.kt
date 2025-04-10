package com.dlfsystems.server

import com.dlfsystems.compiler.Compiler
import com.dlfsystems.server.Yegg.world
import com.dlfsystems.world.Obj

class Connection {

    var buffer = mutableListOf<String>()
    var programming: Pair<String, String>? = null
    var quitRequested = false

    var user: Obj? = null

    // Receive text from websocket.  Just a basic REPL for now.
    fun receiveText(text: String): String {
        programming?.also { verb ->
            if (text == ".") {
                val result = world.programVerb(verb.first, verb.second, buffer.joinToString("\n"))
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
    private fun parseMeta(text: String): String {
        if (text == "@") return ""
        val words = text.split(" ")
        return when (words[0].substring(1, words[0].length)) {
            "connect" -> parseConnect(words)
            "program" -> parseProgram(words)
            "list" -> parseList(words)
            "quit" -> parseQuit(words)
            else -> "I don't understand that."
        }
    }

    private fun parseConnect(words: List<String>): String {
        if (words.size != 3) return "Usage: @connect <name> <password>"
        world.getUserLogin(words[1], words[2])?.also { user ->
            this.user = user
            return "** Connected **"
        }
        return "Bad credentials."
    }

    private fun parseProgram(words: List<String>): String {
        if (words.size < 2) return "@program what?"
        val terms = words[1].split(".")
        if (terms.size != 2) return "@program what?"
        programming = Pair(terms[0], terms[1])
        return "Enter verbcode.  Terminate with '.' on a line by itself."
    }

    private fun parseList(words: List<String>): String {
        if (words.size < 2) return "@list what?"
        val terms = words[1].split(".")
        if (terms.size != 2) return "@list what?"
        return world.listVerb(terms[0], terms[1])
    }

    private fun parseQuit(words: List<String>): String {
        quitRequested = true
        return "Goodbye!"
    }

}
