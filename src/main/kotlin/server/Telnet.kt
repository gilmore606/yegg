package com.dlfsystems.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import java.io.IOException

object Telnet {

    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) throw IllegalStateException("Already started")
        job = Yegg.launch { server() }
    }

    fun stop() {
        job?.cancel()
    }

    private suspend fun server() {
        val serverSocket = aSocket(SelectorManager(Dispatchers.IO))
            .tcp().bind(Yegg.conf.serverAddress, Yegg.conf.serverPort)
        Log.i(TAG, "Server listening at ${serverSocket.localAddress}")

        while (true) {
            val client = serverSocket.accept()
            Log.i(TAG, "Accepted client socket ${client.remoteAddress}")
            TelnetSocket(client)
        }
    }

    class TelnetSocket(val client: Socket) {
        val scope = CoroutineScope(
            Dispatchers.IO + CoroutineName("telnet${client.remoteAddress}")
        )

        init {
            scope.launch {
                val receive = client.openReadChannel()
                val send = client.openWriteChannel(autoFlush = true)

                val conn = Connection { scope.launch { stop() } }

                scope.launch { conn.outputFlow.collect {
                    send.writeStringUtf8("${it.replace("\n", "\r\n")}\r\n")
                } }

                onYeggThread { Yegg.addConnection(conn) }

                try {
                    while (true) {
                        val input = receive.readUTF8Line() ?: break
                        onYeggThread { conn.receiveText(input) }
                    }
                    Log.i(TAG, "Closing client socket ${client.remoteAddress}")
                } catch (e: Throwable) {
                    if (e !is CancellationException) {
                        Log.i(TAG, "Connection error on ${client.remoteAddress}: $e")
                    }
                }

                onYeggThread { Yegg.removeConnection(conn) }
                client.close()
            }
        }

        fun stop() {
            scope.cancel()
        }
    }

    private const val TAG = "Telnet"
}
