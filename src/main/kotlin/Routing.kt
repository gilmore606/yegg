package com.dlfsystems

import com.dlfsystems.compiler.Compiler
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
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
                    result = Yegg.programVerb(traitName, verbName, code)
                }
            }
            call.respondText(result)
        }
    }
}

