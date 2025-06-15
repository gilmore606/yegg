package com.dlfsystems.server

import com.dlfsystems.server.Yegg.launch
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

class Websocket(
    val incoming: ReceiveChannel<Frame>,
    val outgoing: SendChannel<Frame>,
    val close: suspend (CloseReason) -> Unit,
) {
    lateinit var conn: Connection

    suspend fun listen() {
        conn = Connection {
            launch { close(CloseReason(CloseReason.Codes.NORMAL, "server closed")) }
        }
        launch { conn.outputFlow.collect { outgoing.send(Frame.Text(it)) } }

        onYeggThread { Yegg.addConnection(conn) }

        for (frame in incoming) {
            if (frame is Frame.Text) {
                val text = frame.readText()
                onYeggThread { conn.receiveText(text) }
            }
        }
        onYeggThread { Yegg.removeConnection(conn) }
    }

}
