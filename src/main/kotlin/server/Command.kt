package com.dlfsystems.server

import kotlinx.serialization.Serializable

@Serializable
data class Command(
    val names: List<String>,
    val dobj: Arg? = null,
    val prep: Preposition? = null,
    val iobj: Arg? = null,
    val verb: String,
) {
    enum class Arg(val s: String) {
        STRING("string"),   // Literal text
        THIS("this"),       // The object this command is on
        ANY("any"),         // Any object in matching scope
    }

    override fun toString() = names.joinToString("/") + " " +
        (dobj?.s ?: "none") + " " +
        (prep?.strings?.joinToString("/") ?: "none") + " " +
        (iobj?.s ?: "none")

    companion object {
        // Generate a Command from a string representation.
        // "co*mmand/alias [arg] [prep] [arg] = verbName"
        fun fromString(s: String): Command? {
            val SandV = s.split(" = ")
            if (SandV.size != 2) return null
            val verb = SandV[1]
            val NandA = SandV[0].split(" ", limit = 2)
            val names = NandA[0].split("/")
            if (NandA.size == 1) return Command(names, verb = verb)
            val args = NandA[1].split(" ")
            if (args.size > 3) return null

            var dobj: Arg? = null
            var prep: Preposition? = null
            var iobj: Arg? = null
            args.forEach { argstr ->
                Arg.entries.firstOrNull { it.s == argstr }?.also { arg ->
                    if (dobj == null && prep == null)
                        dobj = arg
                    else iobj = arg
                } ?: Preposition.entries.firstOrNull { it.strings.contains(argstr) }?.also {
                    prep = it
                } ?: return null
            }

            // Forbid 'command this any' and other unparseable no-prep formats
            if (prep == null && iobj != null) return null

            return Command(names, dobj, prep, iobj, verb)
        }
    }

}
