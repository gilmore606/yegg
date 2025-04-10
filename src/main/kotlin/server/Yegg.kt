package com.dlfsystems.server

import com.dlfsystems.app.Log
import com.dlfsystems.world.World
import com.dlfsystems.value.VObj
import com.dlfsystems.value.VTrait
import io.viascom.nanoid.NanoId
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.exitProcess

object Yegg {

    val vNullObj = VObj(null)
    val vNullTrait = VTrait(null)

    var logLevel = Log.Level.DEBUG
    var worldName = "world"
    lateinit var world: World

    fun newID() = NanoId.generateOptimized(8, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", 61, 16)

    fun start() {
        val file = File("$worldName.yegg")
        if (file.exists()) {
            Log.i("Loading database from ${file.path}...")
            try {
                // Deserialize the world
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

}
