package com.dlfsystems.server

import kotlinx.serialization.Serializable
import com.dlfsystems.server.Command.Arg.*

@Serializable
class Command(
    val names: List<String>,
    val args: List<Arg>,
    val verb: String,
) {

    @Serializable
    sealed class Arg(val s: String) {
        override fun toString() = s
        data object Text: Arg("text")
        data object This: Arg("this")
        data object Any: Arg("any")
        data object Contents: Arg("contents")
        data object Inventory: Arg("inventory")
        class Prep(val prep: Preposition): Arg(prep.strings.joinToString("/"))
    }

    override fun toString() = names.joinToString("/") + if(args.isEmpty()) "" else args.joinToString(" ", " ")

    companion object {
        // "get/take contents from this"
        // "co*nnect text text"
        fun fromString(s: String): Command? {
            val namesAndArgs = s.split(" ", limit = 2)
            val names = namesAndArgs[0].split("/")
            val verb = "cmd" + names[0].replace("*", "").capitalize()
            val args = mutableListOf<Arg>()
            if (namesAndArgs.size == 1) return Command(names, args, verb)
            var argstr = namesAndArgs[1]
            while (argstr.isNotBlank()) {
                var found = false
                listOf(Text, This, Any, Contents, Inventory).forEach { type ->
                    if (argstr.startsWith(type.s)) {
                        args.add(type)
                        argstr = argstr.substringAfter(" ", "")
                        found = true
                    }
                }
                Preposition.entries.forEach { prep ->
                    prep.strings.forEach { prepStr ->
                        if (argstr.startsWith("$prepStr ")) {
                            args.add(Prep(prep))
                            argstr = argstr.substringAfter("$prepStr ", "")
                            found = true
                        }
                    }
                }
                if (!found) return null
            }
            return Command(names, args, verb)
        }
    }
}
