package com.dlfsystems.yegg.server

import com.dlfsystems.yegg.server.mcp.MCP
import com.dlfsystems.yegg.server.parser.Command
import com.dlfsystems.yegg.util.systemEpoch
import com.dlfsystems.yegg.value.*
import com.dlfsystems.yegg.world.World
import com.dlfsystems.yegg.world.Obj
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.exitProcess

object Yegg {

    @Serializable
    class Conf(
        val worldName: String,
        val serverAddress: String,
        val serverPort: Int,
        val debugMode: Boolean,
        val logLevel: Log.Level,
        val logToConsole: Boolean,
        val optimizeCompiler: Boolean,
        val MSSP: Map<String, String>,
    )

    private const val CONFIG_PATH = "yegg.json"

    private const val CONNECT_MSG = "** Connected **"
    private const val DISCONNECT_MSG = "** Disconnected **"
    const val HUH_MSG = "I don't understand that."
    const val EVAL_OUTPUT_PREFIX = "=> "

    private var inTestMode = false

    private val JSON = Json { allowStructuredMapKeys = true }

    val vTrue = VBool(true)
    val vFalse = VBool(false)
    val vNullObj = VObj(null)
    val vZero = VInt(0)

    lateinit var conf: Conf
    lateinit var world: World

    private val connections = mutableSetOf<Connection>()
    val connectedPlayers = mutableMapOf<Obj, Connection>()

    var startTime: Int = 0

    private val coroutineScope = CoroutineScope(
        SupervisorJob() +
                Dispatchers.Default.limitedParallelism(1) +
                CoroutineName("Yegg")
    )

    fun launch(block: suspend CoroutineScope.() -> Unit) = coroutineScope.launch(block = block)

    suspend fun onThread(block: suspend CoroutineScope.() -> Unit) =
        withContext(coroutineScope.coroutineContext, block)


    fun start(testMode: Boolean = false) {
        inTestMode = testMode
        launch {
            conf = JSON.decodeFromString(File(CONFIG_PATH).readText(Charsets.UTF_8))
            if (!inTestMode) Log.start(conf.worldName)
            if (!inTestMode) loadWorld() else createNewWorld("test")
            MCP.start()
            if (!inTestMode) Telnet.start()
            startTime = systemEpoch()
            Log.i(TAG, "Server started.")
        }
    }

    fun resetForTest() {
        if (!inTestMode) Log.e(TAG, "resetForTest() called when not in test mode!")
        else createNewWorld("test")
    }

    fun stop() {
        Log.i(TAG, "Server shutting down.")
        notifyAll("Server is shutting down!")
        if (!inTestMode) Telnet.stop()
        MCP.stop()
        if (!inTestMode) Log.stop()
        if (!inTestMode) exitProcess(0)
    }

    private fun loadWorld() {
        val worldName = conf.worldName
        val file = File("$worldName.yegg")
        if (file.exists()) {
            Log.i(TAG, "Loading database from ${file.path}...")
            try {
                world = JSON.decodeFromString<World>(file.readText())
                Log.i(TAG, "Loaded ${world.name}.")
            } catch (e: Exception) {
                Log.e(TAG, "FATAL: Failed to load from ${file.path} : $e")
                exitProcess(1)
            }
        } else {
            Log.i(TAG, "No database $worldName found, initializing new world.")
            createNewWorld(worldName)
        }
    }

    private fun createNewWorld(worldName: String) {
        world = World(worldName).apply {
            createTrait("sys")!!.apply {
                addProp("loginBanner", VString("Welcome to Yegg.\n\n"))
                addProp("tickLimit", VInt(100000))
                addProp("stackLimit", VInt(100))
                addProp("callLimit", VInt(50))
                setCommand(Command.fromString("@program string = cmdProgram")!!)
                programVerb("cmdProgram", $$"""
                    [traitName, verbName] = args[0].split(".")
                    trait = $(traitName.replace("\$", ""))
                    notifyConn("Enter code for $trait:$verbName (end with .):")
                    code = readLines()
                    setVerbCode(trait, verbName, code.join("\n"))
                    notifyConn("Verb programmed.")
                """)
                setCommand(Command.fromString("@list string = cmdList")!!)
                programVerb("cmdList", $$"""
                    [traitName, verbName] = args[0].split(".")
                    trait = $(traitName.replace("\$", ""))
                    notifyConn(getVerbCode(trait, verbName))
                """)
                setCommand(Command.fromString("@quit = cmdQuit")!!)
                programVerb("cmdQuit", $$"""
                    disconnectPlayer()
                """)
            }
            val root = createTrait("root")!!.apply {
                addProp("name", VString("thing"))
                addProp("aliases", VList.make(listOf(VString("thing"))))
            }
            createTrait("player")!!.apply {
                addTrait(root)
                addProp("username", VString(""))
                addProp("password", VString(""))
                addProp("lastConnectTime", VInt(0))
                addProp("lastActiveTime", VInt(0))
            }
        }
    }

    fun addConnection(conn: Connection) {
        connections.add(conn)
        conn.sendText(world.getSysValue("loginBanner").asString())
    }

    fun connectPlayer(player: Obj, conn: Connection) {
        conn.player = player
        connectedPlayers[player] = conn
        conn.sendText(CONNECT_MSG)
        Log.i(TAG, "Player $player connected")
        player.setProp("lastConnectTime", VInt(systemEpoch()))
        player.setProp("lastActiveTime", VInt(systemEpoch()))
    }

    fun removeConnection(conn: Connection) {
        if (!connections.contains(conn)) return
        conn.sendText(DISCONNECT_MSG)
        connections.remove(conn)
        connectedPlayers.remove(conn.player)
        conn.onDisconnect()
        Log.i(TAG, "Player ${conn.player} disconnected")
    }

    fun processNextInputs() {
        for (conn in connections) {
            conn.processNextInput()
        }
    }

    fun notifyPlayer(player: Obj?, text: String) {
        connectedPlayers[player]?.sendText(text)
    }

    fun notifyConn(connID: String, text: String) {
        connections.firstOrNull { it.id.id == connID }?.sendText(text)
    }

    fun notifyAll(text: String) {
        connections.forEach { it.sendText(text) }
    }

    fun dumpDatabase(): String {
        val file = File("${world.name}.yegg")
        Log.i(TAG, "Dumping database...")
        try {
            file.writeText(JSON.encodeToString(world))
        } catch (e: Exception) {
            return "ERROR: ${e.message}"
        }
        return ""
    }

    private const val TAG = "Yegg"
}

// Switch execution to the Yegg server thread.
// Use this to modify the World state from other threads.
suspend fun onYeggThread(block: suspend CoroutineScope.() -> Unit) = Yegg.onThread(block)
