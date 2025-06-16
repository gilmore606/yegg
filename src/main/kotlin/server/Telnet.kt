package com.dlfsystems.server

import com.dlfsystems.server.Connection.ColorMode
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException

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
        interface ByteCmd { fun toByte(): Byte }

        enum class Cmd(val v: Int): ByteCmd {
            IAC(255), // initial command marker
            WILL(251),
            WONT(252),
            DO(253),
            DONT(254),
            SB(250),  // subnegotiation start
            SE(240),  // subnegotiation end
            ;
            override fun toByte() = v.toByte()
            companion object {
                fun fromInt(v: Int) = entries.firstOrNull { it.v == v }
            }
        }

        enum class Opt(val v: Int): ByteCmd {
            ECHO(1),         // Remote echo
            SUP_GO_AHEAD(3), // Suppress go-ahead
            STATUS(5),       // Request option status
            TERM_TYPE(24),   // Terminal type
            NAWS(31),        // Negotiate About Window Size
            TERM_SPEED(32),  // Terminal speed
            LINEMODE(34),    // Line mode
            CHARSET(42),     // Character set
            MSSP(70),        // MUD Server Status Protocol
            ;
            override fun toByte() = v.toByte()
            companion object {
                fun fromInt(v: Int) = entries.firstOrNull { it.v == v }
            }
        }

        enum class SubOpt(val v: Int): ByteCmd {
            TERM_TYPE_IS(0),
            TERM_TYPE_SEND(1),
            MSSP_VAR(1),
            MSSP_VAL(2),
            ;
            override fun toByte() = v.toByte()
        }

        enum class MTTSbits(val v: Int) {
            ANSI(1),
            VT100(2),
            UTF8(4),
            COLOR256(8),
            MOUSE(16),
            OSCCOLOR(32),
            SCREENREADER(64),
            PROXY(128),
            TRUECOLOR(256),
            MNES(512),
            MSLP(1024),
            SSL(2048),
        }

        val optPrefs = mutableMapOf<Opt, Boolean>().apply {
            Opt.entries.forEach { set(it, true) }
            set(Opt.STATUS, false)
            set(Opt.TERM_SPEED, false)
            set(Opt.CHARSET, false)
        }
        val optStates = mutableMapOf<Opt, Boolean?>().apply {
            Opt.entries.forEach { set(it, null) }
        }

        val scope = CoroutineScope(
            Dispatchers.IO + CoroutineName("telnet${client.remoteAddress}")
        )

        var receive: ByteReadChannel = client.openReadChannel()
        var send: ByteWriteChannel = client.openWriteChannel(autoFlush = true)
        var conn: Connection

        init {
            conn = Connection { stop() }
            scope.launch { conn.outputFlow.collect { send.sendText(it) } }

            scope.launch {
                onYeggThread { Yegg.addConnection(conn) }
                try {
                    while (true) {
                        while (receive.peekIntByte() == Cmd.IAC.v) {
                            receive.readByte()
                            val command = Cmd.fromInt(receive.readIntByte())
                            val option = Opt.fromInt(receive.readIntByte())
                            if (command == Cmd.SB && option != null && optStates[option] == true) {
                                handleSubnegotiation(option)
                            } else if (command != null && option != null) {
                                handleOptionRequest(command, option)
                            }
                        }
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

            // Send our preference for options not yet negotiated.
            // Then negotiate TERM_TYPE.
            scope.launch {
                optPrefs.keys.forEach { option ->
                    if (optStates[option] == null && optPrefs[option] == true) {
                        sendOption(Cmd.DO, option)
                    }
                }
                requestSubNegotiation(Opt.TERM_TYPE, SubOpt.TERM_TYPE_SEND)  // first response is client name
                requestSubNegotiation(Opt.TERM_TYPE, SubOpt.TERM_TYPE_SEND)  // second response is client type
                requestSubNegotiation(Opt.TERM_TYPE, SubOpt.TERM_TYPE_SEND)  // third response is MTTS bits
            }
        }

        fun stop() {
            client.close()
            scope.cancel()
        }

        private suspend fun handleOptionRequest(command: Cmd, option: Opt) {
            Log.d(TAG, "Got IAC command: $command $option")
            val ourPref = optPrefs[option] ?: false
            val ourState = optStates[option]
            when {
                (command == Cmd.WILL && ourState != true) -> {
                    if (ourPref) updateOption(option, true, Cmd.DO)
                    else updateOption(option, false, Cmd.DONT)
                }
                (command == Cmd.DO && ourState != true) -> {
                    if (ourPref) updateOption(option, true, Cmd.WILL)
                    else updateOption(option, false, Cmd.WONT)
                }
                (command == Cmd.WONT && ourState != false) -> {
                    updateOption(option, false, Cmd.DONT)
                }
                (command == Cmd.DONT && ourState != false) -> {
                    updateOption(option, false, Cmd.WONT)
                }
            }
            if (option == Opt.MSSP && ourState == true) {
                sendMSSP()
            }
        }

        private suspend fun updateOption(option: Opt, state: Boolean, command: Cmd) {
            optStates[option] = state
            sendOption(command, option)
        }

        private suspend fun sendOption(command: Cmd, option: Opt) {
            Log.d(TAG, "Sending IAC option: $command $option")
            send.bytes(Cmd.IAC, command, option)
        }

        private suspend fun requestSubNegotiation(option: Opt, sub: SubOpt) {
            send.bytes(Cmd.IAC, Cmd.SB, option, sub, Cmd.IAC, Cmd.SE)
        }

        private suspend fun sendMSSP() {
            val vars = Yegg.conf.MSSP.toMutableMap().apply {
                set("PLAYERS", Yegg.connectedUsers.size.toString())
                set("UPTIME", Yegg.startTime.toString())
            }
            Log.d(TAG, "Sending MSSP vars: $vars")
            send.bytes(Cmd.IAC, Cmd.SB, Opt.MSSP)
            for (key in vars.keys) {
                send.bytes(SubOpt.MSSP_VAR)
                send.writeString(key)
                send.bytes(SubOpt.MSSP_VAL)
                send.writeString(vars[key]!!)
            }
            send.bytes(Cmd.IAC, Cmd.SE)
        }

        private suspend fun ByteWriteChannel.bytes(vararg bytes: ByteCmd) {
            for (b in bytes) send.writeByte(b.toByte())
        }

        private suspend fun handleSubnegotiation(option: Opt) {
            when (option) {
                Opt.NAWS -> {
                    val width = (receive.readIntByte() * 256) + receive.readIntByte()
                    val height = (receive.readIntByte() * 256) + receive.readIntByte()
                    expectByte(Cmd.IAC) || return
                    expectByte(Cmd.SE) || return
                    Log.d(TAG, "Got new screen size: $width x $height")
                    conn.clientWidth = width
                    conn.clientHeight = height
                }
                Opt.TERM_TYPE -> {
                    expectByte(SubOpt.TERM_TYPE_IS) || return
                    var termString = ""
                    var next = receive.readByte()
                    while (next != Cmd.IAC.toByte()) {
                        termString += next.toChar()
                        next = receive.readByte()
                    }
                    expectByte(Cmd.SE) || return
                    Log.d(TAG, "Got terminal type: $termString")
                    if (termString.startsWith("MTTS ")) {
                        val mtts = termString.substringAfter("MTTS ").toInt()
                        for (bit in MTTSbits.entries) {
                            val value = (mtts and bit.v) > 0
                            when (bit) {
                                MTTSbits.ANSI -> conn.changeColorSupport(ColorMode.ANSI, value)
                                MTTSbits.COLOR256 -> conn.changeColorSupport(ColorMode.XTERM256, value)
                                MTTSbits.OSCCOLOR -> conn.changeColorSupport(ColorMode.OSC, value)
                                MTTSbits.TRUECOLOR -> conn.changeColorSupport(ColorMode.TRUECOLOR, value)
                                MTTSbits.SCREENREADER -> conn.isScreenReader = value
                                MTTSbits.UTF8 -> conn.isUtf8 = value
                                else -> { }
                            }
                        }
                    } else if (termString.contains("xterm", true)) {
                        conn.changeColorSupport(ColorMode.XTERM256, true)
                    } else if (termString.contains("ansi", true)) {
                        conn.changeColorSupport(ColorMode.ANSI, true)
                    }
                }
                else -> Log.w(TAG, "Got subnegotiation for unknown option: $option")
            }
        }

        private suspend fun ByteWriteChannel.sendText(text: String) {
            try { writeStringUtf8("${text.replace("\n", "\r\n")}\r\n") }
            catch (e: Exception) {  }
        }

        private suspend fun expectByte(expected: ByteCmd): Boolean {
            val got = receive.readByte()
            if (got != expected.toByte()) {
                Log.w(TAG, "Protocol error: expected ${expected.toByte().toPosInt()}, but received ${got.toPosInt()}")
                return false
            }
            return true
        }

        private fun Byte.toPosInt() = toUInt().toInt() and 0xFF
        private suspend fun ByteReadChannel.peekIntByte() = peek(1)?.get(0)?.toPosInt()
        private suspend fun ByteReadChannel.readIntByte() = readByte().toPosInt()
    }

    private const val TAG = "Telnet"
}
