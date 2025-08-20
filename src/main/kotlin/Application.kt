package com.dlfsystems.yegg

import com.dlfsystems.yegg.server.Api
import com.dlfsystems.yegg.server.Api.AuthRequest
import com.dlfsystems.yegg.server.Api.COOKIE_NAME
import com.dlfsystems.yegg.server.Websocket
import com.dlfsystems.yegg.server.Yegg
import io.ktor.http.CookieEncoding
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
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

        post("/auth") {
            call.receive<AuthRequest>().also { request ->
                Api.getAuthToken(request.username, request.password)?.also { token ->
                    call.response.cookies.append(COOKIE_NAME, token, CookieEncoding.RAW)
                    call.respondText(token)
                } ?: run {
                    call.respond(Unauthorized)
                }
            }
        }

        get("/verb/{traitName}/{verbName}") {
            if (Api.isAuthorized(call.request)) {
                call.parameters["traitName"]?.also { traitName ->
                    call.parameters["verbName"]?.also { verbName ->
                        Api.getVerbCode(traitName, verbName)?.also { verbCode ->
                            call.respondText(verbCode)
                        } ?: run {
                            call.respond(NotFound)
                        }
                        return@get
                    }
                }
                call.respond(BadRequest)
            } else call.respond(Unauthorized)
        }

        put("/verb/{traitName}/{verbName}") {
            if (Api.isAuthorized(call.request)) {
                val code = call.receiveText()
                call.parameters["traitName"]?.also { traitName ->
                    call.parameters["verbName"]?.also { verbName ->
                        call.respondText(
                            Api.setVerbCode(traitName, verbName, code)
                        )
                        return@put
                    }
                }
                call.respond(BadRequest)
            } else call.respond(Unauthorized)
        }

    }

}
