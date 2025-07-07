package com.dlfsystems.yegg

import com.dlfsystems.yegg.server.Connection
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.server.mcp.MCP
import com.dlfsystems.yegg.server.mcp.Task
import com.dlfsystems.yegg.server.onYeggThread
import com.dlfsystems.yegg.world.trait.Verb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestConnection(scope: CoroutineScope) {

    val output = mutableListOf<String>()

    private val conn = Connection { }

    private val loginBanner = Yegg.world.sys.getProp("loginBanner")?.asString() ?: ""

    init {
        scope.launch { conn.outputFlow.collect { receiveOutput(it) } }
    }

    fun send(text: String) { conn.receiveText(text) }

    private fun receiveOutput(o: String) {
        if (o.isNotBlank() && o != loginBanner) {
            output.add(o)
        }
    }

    suspend fun start() {
        onYeggThread { Yegg.addConnection(conn) }
    }
    suspend fun stop() {
        onYeggThread { Yegg.removeConnection(conn) }
    }

    suspend fun runVerb(source: String) {
        Verb("testVerb").apply {
            program(source)
            MCP.schedule(
                Task.Companion.make(
                exe = this,
                connection = conn,
            ))
        }
        // Wait for MCP to finish running all code.
        while(MCP.taskList().isNotEmpty()) {
            delay(10L)
        }
    }

}