package com.dlfsystems

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.dlfsystems.compiler.Compiler
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

fun Application.configureRouting() {
    routing {
        post("/eval") {
            val code = call.receiveText()
            call.respondText(
                Compiler.eval(code, verbose = true)
            )
        }
        post("/program/{traitName}/{funcName}")  {
            val code = call.receiveText()
            var result = ""
            call.parameters["traitName"]?.also { traitName ->
                call.parameters["funcName"]?.also { funcName ->
                    result = Yegg.programFunc(traitName, funcName, code)
                }
            }
            call.respondText(result)
        }
    }
}

