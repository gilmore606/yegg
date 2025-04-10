package com.dlfsystems.app

import com.dlfsystems.server.Yegg
import com.dlfsystems.compiler.Compiler
import com.dlfsystems.server.Connection
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlin.time.Duration.Companion.seconds

fun Application.configureRouting() {

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {

        webSocket("/ws") { // websocketSession
            val conn = Connection()
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    outgoing.send(
                        Frame.Text(
                        conn.receiveText(text)
                    ))
                    if (conn.quitRequested) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client requested close"))
                    }
                }
            }
        }

        post("/eval") {
            val code = call.receiveText()
            call.respondText(
                Compiler.eval(code, verbose = true)
            )
        }
        post("/program/{traitName}/{verbName}")  {
            val code = call.receiveText()
            var result = ""
            call.parameters["traitName"]?.also { traitName ->
                call.parameters["verbName"]?.also { verbName ->
                    result = Yegg.world.programVerb(traitName, verbName, code)
                }
            }
            call.respondText(result)
        }

    }
}

