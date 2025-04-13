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

        private val ARG_TYPES = listOf(Text, This, Any, Contents, Inventory)

        // Generate a Command from a string representation.
        // "get/take contents from this" (verb = cmdGet)
        // "co*nnect text text" (verb = cmdConnect)
        fun fromString(s: String): Command? {
            val namesAndArgs = s.split(" ", limit = 2)
            val names = namesAndArgs[0].split("/")
            val verb = "cmd" + names[0].replace("*", "").capitalize()
            val args = mutableListOf<Arg>()
            if (namesAndArgs.size == 1) return Command(names, args, verb)
            var argstr = namesAndArgs[1]
            while (argstr.isNotBlank()) {
                var found = false
                ARG_TYPES.firstOrNull { argstr.startsWith(it.s) }?.also { type ->
                    args.add(type)
                    argstr = argstr.substringAfter(" ", "")
                    found = true
                }
                Preposition.entries.forEach { prep ->
                    prep.strings.firstOrNull { argstr.startsWith("$it ") }?.also { prepStr ->
                        args.add(Prep(prep))
                        argstr = argstr.substringAfter("$prepStr ", "")
                        found = true
                    }
                }
                if (!found) return null
            }
            return Command(names, args, verb)
        }
    }
}
