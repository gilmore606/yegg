package com.dlfsystems.server

import com.dlfsystems.server.mcp.MCP
import com.dlfsystems.server.mcp.Task
import com.dlfsystems.server.parser.CommandMatch
import com.dlfsystems.server.parser.Preposition
import com.dlfsystems.util.NanoID
import com.dlfsystems.value.VList
import com.dlfsystems.value.VString
import com.dlfsystems.value.Value
import com.dlfsystems.world.Obj
import com.dlfsystems.world.trait.Verb

// A transport-agnostic connection to the Yegg server.

class Connection(
    private val sendText: (String) -> Unit,
    private val disconnectRemote: () -> Unit,
) {

    @JvmInline
    value class ID(val id: String) { override fun toString() = id }
    val id = ID(NanoID.newID())

    var user: Obj? = null

    private val TAG: String
        get() = "Conn:$id:${user?.id}"

    data class ReadRequest(val forTaskID: Task.ID, val singleLine: Boolean)
    var readRequest: ReadRequest? = null
    val readBuffer = mutableListOf<String>()

    val inputBuffer = mutableListOf<String>()

    fun requestReadLines(forTaskID: Task.ID, singleLine: Boolean) {
        readRequest = ReadRequest(forTaskID, singleLine)
    }

    fun sendText(text: String) { sendText.invoke(text) }

    fun receiveText(text: String) {
        inputBuffer.add(text)
        if (inputBuffer.size > INPUT_BUFFER_MAX_LINES) {
            Log.w(TAG, "Input buffer size exceeded -- forcing disconnect")
            forceDisconnect()
        }
    }

    fun processNextInput() {
        if (inputBuffer.isNotEmpty()) {
            processInput(inputBuffer.removeFirst())
        }
    }

    private fun processInput(text: String) {
        readRequest?.also { readRequest ->
            val singleLine = readRequest.singleLine
            if (text == "." || singleLine) {
                val input: Value = if (singleLine) VString(text)
                    else VList.make(readBuffer.map { VString(it) })
                readBuffer.clear()
                val taskID = readRequest.forTaskID
                this.readRequest = null
                MCP.resumeWithResult(taskID, input)
            } else {
                readBuffer.add(text)
                if (readBuffer.size > INPUT_BUFFER_MAX_LINES) {
                    Log.w(TAG, "Read buffer size exceeded -- forcing disconnect")
                    forceDisconnect()
                }
            }
            return
        }

        if (text.startsWith(";")) {
            // TODO: move this in-DB
            val eval = text.substringAfter(";")
            val source = if (eval.startsWith(";")) "notifyConn(${eval.substringAfter(";")})" else eval
            try {
                val verb = Verb("eval").apply { program(source) }
                MCP.schedule(
                    Task.Companion.make(
                    exe = verb,
                    connection = this,
                ))
            } catch (e: Exception) {
                sendText("E_HUH: ${e.message}")
            }
        } else if (text.isNotBlank()) {
            parseCommand(text)
        }
    }

    fun forceDisconnect() {
        Yegg.removeConnection(this)
        disconnectRemote.invoke()
    }

    fun onDisconnect() {
        readRequest?.also { readRequest ->
            MCP.cancel(readRequest.forTaskID)
        }
    }

    private fun parseCommand(text: String) {
        // Split command into string parts
        val splits = text.split("\\s+".toRegex(), 2)
        val cmdstr = splits[0]
        val argstr = if (splits.size < 2) "" else splits[1]
        var dobjstr = argstr
        var iobjstr = ""
        val prep = Preposition.entries.firstOrNull { p ->
            p.strings.firstOrNull { argstr.contains(it) }?.also { prepstr ->
                dobjstr = argstr.substringBefore(prepstr).trimEnd(' ')
                iobjstr = argstr.substringAfter(prepstr).trimStart(' ')
            } != null
        }

        user?.also { user ->
            // If logged in, match against all objs in scope
            val scope = buildList {
                add(user)
                addAll(user.contentsObjs)
                user.locationObj?.also { loc ->
                    add(loc)
                    addAll(loc.contentsObjs)
                }
            }
            val dobj = matchObj(dobjstr, user, scope)
            val iobj = matchObj(iobjstr, user, scope)
            for (target in scope) {
                target.matchCommand(cmdstr, argstr, dobjstr, dobj, prep, iobjstr, iobj)?.also {
                    runCommand(it)
                    return
                }
            }
        } ?: run {
            // If not logged in, match against $sys
            Yegg.world.sys.matchCommand(null, cmdstr, argstr, dobjstr, null, prep, iobjstr, null)?.also {
                runCommand(it)
                return
            }
        }

        sendText(Yegg.HUH_MSG)
    }

    private fun matchObj(s: String, objMe: Obj?, scope: List<Obj>): Obj? {
        if (s == "me") return objMe
        if (s == "here") return objMe?.locationObj
        // TODO: match against scope when objs actually have names/aliases
        return null
    }

    private fun runCommand(match: CommandMatch) {
        Yegg.world.traits[match.trait.id]?.getVerb(match.verb)?.also { verb ->
            MCP.schedule(
                Task.Companion.make(
                exe = verb,
                args = match.args,
                connection = this,
                vThis = match.obj?.vThis ?: Yegg.vNullObj,
            ))
        } ?: run {
            sendText("ERR: No verb ${match.verb} found for command")
        }
    }

    companion object {
        private const val INPUT_BUFFER_MAX_LINES = 1000
    }
}
