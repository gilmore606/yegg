package com.dlfsystems.server

import com.dlfsystems.compiler.Compiler
import com.dlfsystems.server.Yegg.world
import com.dlfsystems.value.VObj
import com.dlfsystems.value.Value
import com.dlfsystems.world.Obj
import com.dlfsystems.world.ObjID
import com.dlfsystems.world.trait.Trait

class Connection {

    var buffer = mutableListOf<String>()
    var programming: Pair<String, String>? = null
    var quitRequested = false

    var user: Obj? = null

    fun receiveText(text: String): String {
        programming?.also { verb ->
            if (text == ".") {
                val result = world.programVerb(verb.first, verb.second, buffer.joinToString("\n"))
                buffer.clear()
                programming = null
                return result
            } else buffer.add(text)
        } ?: run {
            return if (text.startsWith(";")) {
                Compiler.eval(text.substringAfter(";"), connection = this)
            } else if (text.startsWith("@")) {
                parseMeta(text)
            } else {
                parseCommand(text)
            }
        }
        return ""
    }

    private fun parseCommand(text: String): String {
        val words = text.split("\\s+".toRegex())

        // send: ["put", "blue cube", "in", "this"]
        // send: ["put", #OBJ1, "in", #OBJ2]
        // receive: "put", $trait, #OBJ2, [#OBJ1]

        // send: ["con", "gilmore", "bluguuru"]
        // send: ["con", "gilmore", "bluguuru"]
        // receive: "connect", $sys, #-1, ["gilmore", "bluguuru"]

        // send: ["l"]
        // send: ["l"]
        // receive: "look", $trait, #OBJ, []

        // TODO: pre-parse for prep, objs
        user?.also { user ->
            buildList {
                add(user)
                addAll(user.contentsObjs)
                user.locationObj?.also { loc ->
                    add(loc)
                    addAll(loc.contentsObjs)
                }
            }.forEach { target ->
                target.matchCommand(words)?.also {
                    return runCommand(it)
                }
            }
            return "I don't understand that."
        }

        world.getTrait("sys")!!.matchCommand(words)?.also {
            return runCommand(it)
        }

        return "I don't understand that."
    }

    private fun runCommand(match: CommandMatch): String {

        return "SHIT!"
    }

    // Respond to meta commands.
    // TODO: this should all go away and be replaced by in-DB verbs (probably).  Leaving in for expedience.
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
