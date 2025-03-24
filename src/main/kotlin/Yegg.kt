package com.dlfsystems

import com.dlfsystems.app.Log
import com.dlfsystems.world.World
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.value.VObj
import com.dlfsystems.value.VTrait
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.exitProcess

object Yegg {

    // TODO: users, auth, *waves hands*
    class Connection() {

        var buffer = mutableListOf<String>()
        var programming: Pair<String, String>? = null
        var quitRequested = false

        // Receive text from websocket.  Just a basic REPL for now.
        fun receiveText(text: String): String {
            programming?.also { verb ->
                if (text == ".") {
                    val result = programVerb(verb.first, verb.second, buffer.joinToString("\n"))
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
                "list" -> parseList(words)
                "quit" -> parseQuit(words)
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

        fun parseList(words: List<String>): String {
            if (words.size < 2) return "@list what?"
            val terms = words[1].split(".")
            if (terms.size != 2) return "@list what?"
            return world.listVerb(terms[0], terms[1])
        }

        fun parseQuit(words: List<String>): String {
            quitRequested = true
            return "Goodbye!"
        }
    }

    val vNullObj = VObj(null)
    val vNullTrait = VTrait(null)

    var logLevel = Log.Level.DEBUG
    var worldName = "world"
    lateinit var world: World

    fun start() {
        val file = File("$worldName.yegg")
        if (file.exists()) {
            Log.i("Loading database from ${file.path}...")
            try {
                world = Json.decodeFromString<World>(file.readText())
                Log.i("Loaded ${world.name} with ${world.traits.size} traits and ${world.objs.size} objs.")
            } catch (e: Exception) {
                Log.e("FATAL: Failed to load from ${file.path} !")
                exitProcess(1)
            }
        } else {
            Log.i("No database $worldName found, initializing new world.")
            world = World(worldName).apply {
                addTrait("sys")
                addTrait("user")
            }
        }
    }

    fun dumpDatabase(): String? {
        val file = File("${world.name}.yegg")
        Log.i("Dumping database...")
        try {
            file.writeText(Json.encodeToString(world))
        } catch (e: Exception) {
            return "ERROR: ${e.message}"
        }
        return null
    }

    fun programVerb(traitName: String, name: String, code: String): String = world.programVerb(traitName, name, code)

}
