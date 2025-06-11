package com.dlfsystems.world.trait

import com.dlfsystems.server.Log
import com.dlfsystems.server.parser.Command
import com.dlfsystems.server.Yegg
import com.dlfsystems.server.mcp.MCP
import com.dlfsystems.util.systemEpoch
import com.dlfsystems.value.*
import com.dlfsystems.vm.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

// A special trait which exists in every world.
// Provides environment properties, server control, and primitive functions.
// Should never be attached to an Obj.

@Serializable
@SerialName("SysTrait")
class SysTrait : Trait("sys") {

    override fun getProp(propName: String): Value? {
        when (propName) {
            "time" -> return propTime()
            "connectedUsers" -> return propConnectedUsers()
            "tasks" -> return propTasks()
        }
        return super.getProp(propName)
    }

    override fun callStaticVerb(c: Context, verbName: String, args: List<Value>): Value? {
        when (verbName) {
            "connectUser" -> return verbConnectUser(c, args)
            "disconnectUser" -> return verbDisconnectUser(c, args)
            "notify" -> return verbNotify(args)
            "notifyConn" -> return verbNotifyConn(c, args)
            "createTrait" -> return verbCreateTrait(args)
            "destroyTrait" -> return verbDestroyTrait(args)
            "addParent" -> return verbAddParent(args)
            "removeParent" -> return verbRemoveParent(args)
            "addProp" -> return verbAddProp(args)
            "removeProp" -> return verbRemoveProp(args)
            "clearProp" -> return verbClearProp(args)
            "create" -> return verbCreate(args)
            "destroy" -> return verbDestroy(args)
            "move" -> return verbMove(args)
            "random" -> return verbRandom(args)
            "chance" -> return verbChance(args)
            "min" -> return verbMin(args)
            "max" -> return verbMax(args)
            "setCommand" -> return verbSetCommand(args)
            "removeCommand" -> return verbRemoveCommand(args)
            "getVerbCode" -> return verbGetVerbCode(args)
            "setVerbCode" -> return verbSetVerbCode(args)
            "removeVerb" -> return verbRemoveVerb(args)
            "dumpDatabase" -> return verbDumpDatabase(args)
            "shutdownServer" -> return verbShutdownServer(args)
        }
        return super.callStaticVerb(c, verbName, args)
    }

    // $sys.time -> n
    private fun propTime() = VInt(systemEpoch())

    // $sys.connectedUsers -> [#obj, #obj...]
    private fun propConnectedUsers() = VList.make(Yegg.connectedUsers.keys.map { it.vThis })

    // $sys.tasks ->
    private fun propTasks() = VList.make(MCP.taskList().map { it.vID })

    // $sys.connectUser("username", "password") -> true if connected
    private fun verbConnectUser(c: Context, args: List<Value>): VBool {
        if (args.size != 2 || args[0] !is VString || args[1] !is VString) throw IllegalArgumentException("Bad args for connectUser")
        Yegg.world.getUserLogin((args[0] as VString).v, (args[1] as VString).v)?.also { user ->
            c.connection?.also {
                Yegg.connectUser(user, it)
                c.vUser = user.vThis
            }
            return Yegg.vTrue
        }
        return Yegg.vFalse
    }

    // $sys.disconnectUser()
    private fun verbDisconnectUser(c: Context, args: List<Value>): VVoid {
        if (args.isNotEmpty()) throw IllegalArgumentException("Bad args for disconnectUser")
        c.connection?.quitRequested = true
        return VVoid
    }

    // $sys.notify(#obj, "text")
    private fun verbNotify(args: List<Value>): VVoid {
        if (args.size != 2 || args[0] !is VObj || args[1] !is VString) throw IllegalArgumentException("Bad args for notify")
        Yegg.notifyUser((args[0] as VObj).obj(), (args[1] as VString).v)
        return VVoid
    }

    // $sys.notifyConn("text")
    private fun verbNotifyConn(c: Context, args: List<Value>): VVoid {
        if (args.size != 1) throw IllegalArgumentException("Bad args for notifyConn")
        c.connection?.id?.id?.also { Yegg.notifyConn(it, args[0].asString()) }
        return VVoid
    }

    // $sys.createTrait("newTrait")
    private fun verbCreateTrait(args: List<Value>): VVoid {
        if (args.size != 1 || args[0] !is VString) throw IllegalArgumentException("Bad args for createTrait")
        Yegg.world.createTrait(args[0].asString())
        return VVoid
    }

    // $sys.destroyTrait("trait")
    private fun verbDestroyTrait(args: List<Value>): VVoid {
        if (args.size != 1) throw IllegalArgumentException("Bad args for destroyTrait")
        Yegg.world.destroyTrait(args[0].asString())
        return VVoid
    }

    // $sys.create($trait1, $trait2...) -> #obj
    private fun verbCreate(args: List<Value>): VObj {
        val obj = Yegg.world.createObj()
        Yegg.world.getTrait("root")?.also { obj.addTrait(it) }
        try {
            args.forEach {
                if (it !is VTrait) throw IllegalArgumentException("Non-trait passed to create")
                obj.addTrait(it.trait()!!)
            }
            return obj.vThis
        } catch (e: Exception) {
            Yegg.world.destroyObj(obj)
            throw e
        }
    }

    // $sys.destroy(#obj)
    private fun verbDestroy(args: List<Value>): VVoid {
        if (args.size != 1 || args[0] !is VObj) throw IllegalArgumentException("Bad args for destroy")
        (args[0] as VObj).obj()?.also { subject ->
            Yegg.world.destroyObj(subject)
        } ?: throw IllegalArgumentException("invalid obj")
        return VVoid
    }

    // $sys.addParent($trait, $parentTrait) / (obj, $parentTrait)
    private fun verbAddParent(args: List<Value>): VVoid {
        if (args.size != 2 || args[1] !is VTrait) throw IllegalArgumentException("Bad args for addParent")
        when (args[0]) {
            is VTrait -> (args[0] as VTrait).trait()!!.addTrait((args[1] as VTrait).trait()!!)
            is VObj -> (args[0] as VObj).obj()!!.addTrait((args[1] as VTrait).trait()!!)
            else -> throw IllegalArgumentException("cannot addParent to ${args[0].type}")
        }
        return VVoid
    }

    // $sys.removeParent($trait, $parentTrait) / (obj, $parentTrait)
    private fun verbRemoveParent(args: List<Value>): VVoid {
        if (args.size != 2 || args[1] !is VTrait) throw IllegalArgumentException("Bad args for removeParent")
        when (args[0]) {
            is VTrait -> (args[0] as VTrait).trait()!!.removeTrait((args[1] as VTrait).trait()!!)
            is VObj -> (args[0] as VObj).obj()!!.removeTrait((args[1] as VTrait).trait()!!)
            else -> throw IllegalArgumentException("cannot removeParent from ${args[0].type}")
        }
        return VVoid
    }

    // $sys.move(#obj, #loc)
    private fun verbMove(args: List<Value>): VVoid {
        if (args.size != 2 || args[0] !is VObj || args[1] !is VObj) throw IllegalArgumentException("Bad args for move")
        (args[0] as VObj).obj()?.also { subject ->
            Yegg.world.moveObj(subject, args[1] as VObj)
        } ?: throw IllegalArgumentException("invalid obj")
        return VVoid
    }

    // $sys.random() -> float from 0.0 until 1.0
    // $sys.random(x) -> int from 0 until x (exc)
    // $sys.random(x, y) -> int from x to y (inc)
    private fun verbRandom(args: List<Value>): Value {
        when (args.size) {
            0 -> return VFloat(Random.nextFloat())
            1 -> {
                val x = (args[0] as? VInt)?.v ?: throw IllegalArgumentException("Bad arg to random")
                return VInt(Random.nextInt(x))
            }
            2 -> {
                val x = (args[0] as? VInt)?.v ?: throw IllegalArgumentException("Bad arg to random")
                val y = (args[1] as? VInt)?.v ?: throw IllegalArgumentException("Bad arg to random")
                return VInt(Random.nextInt(x, y + 1))
            }
        }
        throw IllegalArgumentException("Too many args to random")
    }

    // $sys.chance() -> 50-50 true/false
    // $sys.chance(float) -> true float fraction of the time
    private fun verbChance(args: List<Value>): Value {
        if (args.isEmpty()) return VBool(Random.nextBoolean())
        if (args.size != 1) throw IllegalArgumentException("Bad args for chance")
        val x = (args[0] as? VFloat)?.v ?: throw IllegalArgumentException("Bad arg to chance")
        return VBool(Random.nextFloat() < x)
    }

    // $sys.min(v1, v2, v3...) -> lowest INT/FLOAT value in args (or list, if only arg is list)
    private fun verbMin(args: List<Value>): Value {
        if (args.size < 1) throw IllegalArgumentException("Bad args for min")
        var numbers = args
        if (args.size == 1) {
            if (args[0].type == Value.Type.LIST) numbers = (args[0] as VList).v
        }
        if (numbers[0].numericValue() == null) throw IllegalArgumentException("Bad args for min")
        var min = numbers[0]
        var minval = numbers[0].numericValue()!!
        numbers.forEach {
            it.numericValue()?.also { itval ->
                if (itval < minval) {
                    min = it
                    minval = itval
                }
            }
        }
        return min
    }

    // $sys.max(v1, v2, v3...) -> highest INT/FLOAT value in args (or list, if only arg is list)
    private fun verbMax(args: List<Value>): Value {
        if (args.size < 1) throw IllegalArgumentException("Bad args for max")
        var numbers = args
        if (args.size == 1) {
            if (args[0].type == Value.Type.LIST) numbers = (args[0] as VList).v
        }
        if (numbers[0].numericValue() == null) throw IllegalArgumentException("Bad args for max")
        var max = numbers[0]
        var maxval = numbers[0].numericValue()!!
        numbers.forEach {
            it.numericValue()?.also { itval ->
                if (itval > maxval) {
                    max = it
                    maxval = itval
                }
            }
        }
        return max
    }

    private fun Value.numericValue(): Float? = when (type) {
        Value.Type.INT -> (this as VInt).v.toFloat()
        Value.Type.FLOAT -> (this as VFloat).v
        else -> null
    }

    // $sys.setCommand($trait, "co*mmand/cmd arg prep arg = cmdVerb") -> "cmdVerb"
    private fun verbSetCommand(args: List<Value>): VVoid {
        if (args.size != 2 || args[0] !is VTrait || args[1] !is VString) throw IllegalArgumentException("Bad args for setCommand")
        Yegg.world.traits[(args[0] as VTrait).v]?.also { trait ->
            Command.fromString((args[1] as VString).v)?.also { command ->
                trait.setCommand(command)
                Log.d("setCommand($trait, $command)")
                return VVoid
            } ?: throw IllegalArgumentException("invalid command pattern")
        }
        throw IllegalArgumentException("invalid trait")
    }

    // $sys.removeCommand($trait, "co*mmand/cmd arg prep arg")
    private fun verbRemoveCommand(args: List<Value>): VVoid {
        if (args.size != 2 || args[0] !is VTrait || args[1] !is VString) throw IllegalArgumentException("Bad args for removeCommand")
        Yegg.world.traits[(args[0] as VTrait).v]?.also { trait ->
            trait.removeCommand((args[1] as VString).v)
            Log.d("removeCommand($trait, ${args[1]}")
            return VVoid
        }
        throw IllegalArgumentException("invalid trait")
    }

    // $sys.getVerbCode($trait, "verb") -> "string of source code"
    private fun verbGetVerbCode(args: List<Value>): VString {
        if (args.size != 2 || args[0] !is VTrait || args[1] !is VString) throw IllegalArgumentException("Bad args for getVerbCode")
        (args[0] as VTrait).trait()?.getVerb((args[1] as VString).v)?.also { verb ->
            return VString(verb.source)
        } ?: throw IllegalArgumentException("verb not found")
    }

    // $sys.setVerbCode($trait, "verb", "source code")
    private fun verbSetVerbCode(args: List<Value>): VVoid {
        if (args.size != 3 || args[0] !is VTrait || args[1] !is VString || args[2] !is VString) throw IllegalArgumentException("Bad args for setVerbCode")
        (args[0] as VTrait).trait()?.also { trait ->
            trait.programVerb((args[1] as VString).v, (args[2] as VString).v)
        } ?: throw IllegalArgumentException("invalid trait")
        return VVoid
    }

    // $sys.removeVerb($trait, "verb")
    private fun verbRemoveVerb(args: List<Value>): VVoid {
        if (args.size != 2 || args[0] !is VTrait || args[1] !is VString) throw IllegalArgumentException("Bad args for removeVerb")
        Yegg.world.traits[(args[0] as VTrait).v]?.also { trait ->
            trait.removeVerb((args[1] as VString).v)
            Log.d("removeVerb($trait, ${args[1]}")
            return VVoid
        }
        throw IllegalArgumentException("invalid trait")
    }

    // $sys.dumpDatabase() -> "err"
    private fun verbDumpDatabase(args: List<Value>): VString {
        if (args.isNotEmpty()) throw IllegalArgumentException("Bad args for dumpDatabase")
        return VString(Yegg.dumpDatabase())
    }

    // $sys.shutdownServer()
    private fun verbShutdownServer(args: List<Value>): VVoid {
        Log.i("Shutdown requested: $args")
        Yegg.stop()
        return VVoid
    }

    // $sys.addProp($trait, "propName", value)
    private fun verbAddProp(args: List<Value>): VVoid {
        if (args.size != 3) throw IllegalArgumentException("Bad args for addProp")
        if (args[0].type != Value.Type.TRAIT) throw IllegalArgumentException("first arg is ${args[0].type} not TRAIT")
        if (args[1].type != Value.Type.STRING) throw IllegalArgumentException("second arg is ${args[1].type} not STRING")
        (args[0] as VTrait).trait()?.addProp(args[1].asString(), args[2])
            ?: throw IllegalArgumentException("invalid trait")
        return VVoid
    }

    // $sys.removeProp($trait, "propName")
    private fun verbRemoveProp(args: List<Value>): VVoid {
        if (args.size != 2) throw IllegalArgumentException("Bad args for removeProp")
        if (args[0].type != Value.Type.TRAIT) throw IllegalArgumentException("first arg is ${args[0].type} not TRAIT")
        if (args[1].type != Value.Type.STRING) throw IllegalArgumentException("second arg is ${args[1].type} not STRING")
        (args[0] as VTrait).trait()?.removeProp(args[1].asString())
            ?: throw IllegalArgumentException("invalid trait")
        return VVoid
    }

    // $sys.clearProp($trait, "propName") / (obj, "propName")
    private fun verbClearProp(args: List<Value>): VVoid {
        if (args.size != 2 || args[1].type != Value.Type.STRING) throw IllegalArgumentException("Bad args for clearProp")
        when (args[0]) {
            is VTrait -> (args[0] as VTrait).trait()!!.clearProp(args[1].asString())
            is VObj -> {

            }
            else -> throw IllegalArgumentException("cannot clearProp on ${args[0].type}")
        }
        return VVoid
    }
}
