package com.dlfsystems.server

import com.dlfsystems.server.mcp.MCP
import com.dlfsystems.server.mcp.Task
import com.dlfsystems.server.parser.Connection
import com.dlfsystems.world.trait.Verb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestConnection(scope: CoroutineScope) {

    val output = mutableListOf<String>()
    private val conn = Connection { scope.launch { this@TestConnection.receiveOutput(it) } }

    fun send(text: String) { conn.receiveText(text) }
    private fun receiveOutput(o: String) { if (o.isNotBlank()) output.add(o) }

    suspend fun start() { onYeggThread { Yegg.addConnection(conn) } }
    suspend fun stop() { onYeggThread { Yegg.removeConnection(conn) } }

    suspend fun runVerb(source: String) {
        Verb("testVerb").apply {
            program(source)
            MCP.schedule(Task.make(
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
