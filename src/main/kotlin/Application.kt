package com.dlfsystems.yegg

import com.dlfsystems.yegg.server.Websocket
import com.dlfsystems.yegg.server.Yegg
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    Yegg.start()

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/ws") {
            Websocket(incoming, outgoing) { close(it) }.listen()
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
