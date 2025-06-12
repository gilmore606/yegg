package com.dlfsystems.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
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
            val address = client.remoteAddress
            Log.i(TAG, "Accepted client socket $address")

            val scope = CoroutineScope(
                Dispatchers.IO.limitedParallelism(2) + CoroutineName("telnet$address")
            )
            scope.launch {
                val receive = client.openReadChannel()
                val send = client.openWriteChannel(autoFlush = true)

                val conn = Connection {
                    scope.launch {
                        try {
                            send.writeStringUtf8("${it.replace("\n", "\r\n")}\r\n")
                        } catch (e: IOException) { }
                    }
                }
                onYeggThread { Yegg.addConnection(conn) }

                try {
                    while (true) {
                        val input = receive.readUTF8Line() ?: break
                        onYeggThread { conn.receiveText(input) }
                        if (conn.quitRequested) break
                    }
                    Log.i(TAG, "Closing client socket $address")
                } catch (e: Throwable) {
                    Log.i(TAG, "Connection error on $address: $e")
                }

                onYeggThread { Yegg.removeConnection(conn) }
                client.close()
            }

        }
    }

    private const val TAG = "Telnet"
}
