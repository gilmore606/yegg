package com.dlfsystems.yegg.world.trait

import com.dlfsystems.yegg.server.Log
import com.dlfsystems.yegg.server.parser.Command
import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.server.mcp.MCP
import com.dlfsystems.yegg.util.fail
import com.dlfsystems.yegg.util.systemEpoch
import com.dlfsystems.yegg.value.*
import com.dlfsystems.yegg.value.Value.Type.*
import com.dlfsystems.yegg.vm.Context
import com.dlfsystems.yegg.vm.VMException
import com.dlfsystems.yegg.vm.VMException.Type.E_INVARG
import com.dlfsystems.yegg.vm.VMException.Type.E_INVOBJ
import com.dlfsystems.yegg.vm.VMException.Type.E_TYPE
import com.dlfsystems.yegg.vm.VMException.Type.E_VERBNF
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
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
            "connectedPlayers" -> return propConnectedPlayers()
            "tasks" -> return propTasks()
            "allTraits" -> return propAllTraits()
        }
        return super.getProp(propName)
    }

    override fun callStaticVerb(c: Context, verbName: String, args: List<Value>): Value? {
        when (verbName) {
            "connectPlayer" -> return verbConnectPlayer(c, args)
            "disconnectPlayer" -> return verbDisconnectPlayer(c, args)
            "notify" -> return verbNotify(args)
            "cnotify" -> return verbCnotify(c, args)
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
            "ansi" -> return verbAnsi(args)
            "utf8" -> return verbUtf8(args)
            "cp437" -> return verbCp437(args)
            "crypt" -> return verbCrypt(args)
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

    // $sys.connectedPlayers -> [#obj, #obj...]
    private fun propConnectedPlayers() = VList.make(Yegg.connectedPlayers.keys.map { it.vThis })

    // $sys.tasks ->
    private fun propTasks() = VList.make(MCP.taskList().map { it.vID })

    // $sys.allTraits -> [$sys, $player, ...]
    private fun propAllTraits() = VList.make(Yegg.world.traits.keys.map { VTrait(it) })

    // $sys.connectPlayer("username", "password") -> true if connected
    private fun verbConnectPlayer(c: Context, args: List<Value>): VBool {
        requireArgTypes(args, STRING, STRING)
        Yegg.world.getPlayerLogin((args[0] as VString).v, (args[1] as VString).v)?.also { player ->
            c.connection?.also {
                Yegg.connectPlayer(player, it)
                c.vPlayer = player.vThis
            }
            return Yegg.vTrue
        }
        return Yegg.vFalse
    }

    // $sys.disconnectPlayer()
    private fun verbDisconnectPlayer(c: Context, args: List<Value>): VVoid {
        requireArgTypes(args)
        c.connection?.forceDisconnect()
        return VVoid
    }

    // $sys.notify(#obj, "text")
    private fun verbNotify(args: List<Value>): VVoid {
        requireArgTypes(args, OBJ, null)
        Yegg.notifyPlayer((args[0] as VObj).obj(), args[1].asString())
        return VVoid
    }

    // $sys.cnotify("text")
    private fun verbCnotify(c: Context, args: List<Value>): VVoid {
        requireArgTypes(args, null)
        c.connection?.id?.id?.also { Yegg.notifyConn(it, args[0].asString()) }
        return VVoid
    }

    // $sys.createTrait("newTrait")
    private fun verbCreateTrait(args: List<Value>): VVoid {
        requireArgTypes(args, STRING)
        Yegg.world.createTrait(args[0].asString())
        return VVoid
    }

    // $sys.destroyTrait("trait")
    private fun verbDestroyTrait(args: List<Value>): VVoid {
        requireArgTypes(args, STRING)
        Yegg.world.destroyTrait(args[0].asString())
        return VVoid
    }

    // $sys.create($trait1, $trait2...) -> #obj
    private fun verbCreate(args: List<Value>): VObj {
        val obj = Yegg.world.createObj()
        Yegg.world.getTrait("root")?.also { obj.addTrait(it) }
        try {
            args.forEach {
                if (it !is VTrait) fail(E_INVARG, "Non-trait passed to create")
                obj.addTrait((it as VTrait).trait()!!)
            }
            return obj.vThis
        } catch (e: Exception) {
            Yegg.world.destroyObj(obj)
            throw e
        }
    }

    // $sys.destroy(#obj)
    private fun verbDestroy(args: List<Value>): VVoid {
        requireArgTypes(args, OBJ)
        (args[0] as VObj).obj()?.also { subject ->
            Yegg.world.destroyObj(subject)
        } ?: fail(E_INVOBJ, "invalid obj")
        return VVoid
    }

    // $sys.addParent($trait, $parentTrait) / (obj, $parentTrait)
    private fun verbAddParent(args: List<Value>): VVoid {
        requireArgTypes(args, null, TRAIT)
        when (args[0]) {
            is VTrait -> (args[0] as VTrait).trait()!!.addTrait((args[1] as VTrait).trait()!!)
            is VObj -> (args[0] as VObj).obj()!!.addTrait((args[1] as VTrait).trait()!!)
            else -> fail(E_TYPE, "cannot addParent to ${args[0].type}")
        }
        return VVoid
    }

    // $sys.removeParent($trait, $parentTrait) / (obj, $parentTrait)
    private fun verbRemoveParent(args: List<Value>): VVoid {
        requireArgTypes(args, null, TRAIT)
        when (args[0]) {
            is VTrait -> (args[0] as VTrait).trait()!!.removeTrait((args[1] as VTrait).trait()!!)
            is VObj -> (args[0] as VObj).obj()!!.removeTrait((args[1] as VTrait).trait()!!)
            else -> fail(E_TYPE, "cannot removeParent from ${args[0].type}")
        }
        return VVoid
    }

    // $sys.move(#obj, #loc)
    private fun verbMove(args: List<Value>): VVoid {
        requireArgTypes(args, OBJ, OBJ)
        (args[0] as VObj).obj()?.also { subject ->
            Yegg.world.moveObj(subject, args[1] as VObj)
        } ?: fail(E_INVOBJ, "invalid obj")
        return VVoid
    }

    // $sys.random() -> float from 0.0 until 1.0
    // $sys.random(x) -> int from 0 until x (exc)
    // $sys.random(x, y) -> int from x to y (inc)
    private fun verbRandom(args: List<Value>): Value {
        when (args.size) {
            0 -> return VFloat(Random.nextFloat())
            1 -> {
                val x = (args[0] as? VInt)?.v ?: throw VMException(E_TYPE, "Bad arg to random")
                return VInt(Random.nextInt(x))
            }
            2 -> {
                val x = (args[0] as? VInt)?.v ?: throw VMException(E_TYPE, "Bad arg to random")
                val y = (args[1] as? VInt)?.v ?: throw VMException(E_TYPE, "Bad arg to random")
                return VInt(Random.nextInt(x, y + 1))
            }
        }
        throw VMException(E_INVARG, "Too many args to random")
    }

    // $sys.chance() -> 50-50 true/false
    // $sys.chance(float) -> true float fraction of the time
    private fun verbChance(args: List<Value>): Value {
        if (args.isEmpty()) return VBool(Random.nextBoolean())
        requireArgTypes(args, FLOAT)
        val x = (args[0] as VFloat).v
        return VBool(Random.nextFloat() < x)
    }

    // $sys.min(v1, v2, v3...) -> lowest INT/FLOAT value in args (or list, if only arg is list)
    private fun verbMin(args: List<Value>): Value {
        if (args.isEmpty()) fail(E_INVARG, "Bad args for min")
        var numbers = args
        if (args.size == 1) {
            if (args[0].type == LIST) numbers = (args[0] as VList).v
        }
        if (numbers[0].numericValue() == null) fail(E_INVARG, "Bad args for min")
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
        if (args.isEmpty()) fail(E_INVARG, "Bad args for max")
        var numbers = args
        if (args.size == 1) {
            if (args[0].type == LIST) numbers = (args[0] as VList).v
        }
        if (numbers[0].numericValue() == null) fail(E_INVARG, "Bad args for max")
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

    // $sys.ansi("code") -> code surrounded by ANSI escape
    private fun verbAnsi(args: List<Value>): VString {
        requireArgTypes(args, STRING)
        return VString('\u001b' + "[" + args[0].asString() + "m")
    }

    // $sys.utf8(int) -> one-char string with UTF8 code int
    private fun verbUtf8(args: List<Value>): VString {
        requireArgTypes(args, INT)
        return VString(Char((args[0] as VInt).v).toString())
    }

    // $sys.cp437(int) -> one-char string with UTF8 equivalent of CP437 code int
    private fun verbCp437(args: List<Value>): VString {
        requireArgTypes(args, INT)
        val incode = (args[0] as VInt).v
        if (incode < 0 || incode > 255) fail(E_INVARG, "cp437 code must be between 0 and 255")
        return VString(Char(CP437_TO_UTF8_TABLE[incode]).toString())
    }

    // $sys.crypt("plaintext") -> bcrypted salted text
    private fun verbCrypt(args: List<Value>): VString {
        requireArgTypes(args, STRING)
        val plain = (args[0] as VString).v
        return VString(BCrypt.hashpw(plain, BCrypt.gensalt()))
    }

    private fun Value.numericValue(): Float? = when (type) {
        INT -> (this as VInt).v.toFloat()
        FLOAT -> (this as VFloat).v
        else -> null
    }

    // $sys.setCommand($trait, "co*mmand/cmd arg prep arg = cmdVerb") -> "cmdVerb"
    private fun verbSetCommand(args: List<Value>): VVoid {
        requireArgTypes(args, TRAIT, STRING)
        Yegg.world.traits[(args[0] as VTrait).v]?.also { trait ->
            Command.fromString((args[1] as VString).v)?.also { command ->
                trait.setCommand(command)
                return VVoid
            } ?: fail(E_INVARG, "invalid command pattern")
        }
        throw VMException(E_INVARG, "invalid trait")
    }

    // $sys.removeCommand($trait, "co*mmand/cmd arg prep arg")
    private fun verbRemoveCommand(args: List<Value>): VVoid {
        requireArgTypes(args, TRAIT, STRING)
        Yegg.world.traits[(args[0] as VTrait).v]?.also { trait ->
            trait.removeCommand((args[1] as VString).v)
            return VVoid
        }
        throw VMException(E_INVARG, "invalid trait")
    }

    // $sys.getVerbCode($trait, "verb") -> "string of source code"
    private fun verbGetVerbCode(args: List<Value>): VString {
        requireArgTypes(args, TRAIT, STRING)
        (args[0] as VTrait).trait()?.getVerb((args[1] as VString).v)?.also { verb ->
            return VString(verb.source)
        } ?: throw VMException(E_VERBNF, "verb not found")
    }

    // $sys.setVerbCode($trait, "verb", "source code")
    private fun verbSetVerbCode(args: List<Value>): VVoid {
        requireArgTypes(args, TRAIT, STRING, STRING)
        (args[0] as VTrait).trait()?.also { trait ->
            trait.programVerb((args[1] as VString).v, (args[2] as VString).v)
        } ?: fail(E_INVARG, "invalid trait")
        return VVoid
    }

    // $sys.removeVerb($trait, "verb")
    private fun verbRemoveVerb(args: List<Value>): VVoid {
        requireArgTypes(args, TRAIT, STRING)
        Yegg.world.traits[(args[0] as VTrait).v]?.also { trait ->
            trait.removeVerb((args[1] as VString).v)
            return VVoid
        }
        throw VMException(E_INVARG, "invalid trait")
    }

    // $sys.dumpDatabase() -> "err"
    private fun verbDumpDatabase(args: List<Value>): VString {
        requireArgTypes(args)
        return VString(Yegg.dumpDatabase())
    }

    // $sys.shutdownServer()
    private fun verbShutdownServer(args: List<Value>): VVoid {
        Log.i(TAG, "Shutdown requested: $args")
        Yegg.stop()
        return VVoid
    }

    // $sys.addProp($trait, "propName", value)
    private fun verbAddProp(args: List<Value>): VVoid {
        requireArgTypes(args, TRAIT, STRING, null)
        (args[0] as VTrait).trait()?.addProp(args[1].asString(), args[2])
            ?: fail(E_INVARG, "invalid trait")
        return VVoid
    }

    // $sys.removeProp($trait, "propName")
    private fun verbRemoveProp(args: List<Value>): VVoid {
        requireArgTypes(args, TRAIT, STRING)
        (args[0] as VTrait).trait()?.removeProp(args[1].asString())
            ?: fail(E_INVARG, "invalid trait")
        return VVoid
    }

    // $sys.clearProp($trait, "propName") / (obj, "propName")
    private fun verbClearProp(args: List<Value>): VVoid {
        requireArgTypes(args, null, STRING)
        when (args[0]) {
            is VTrait -> (args[0] as VTrait).trait()!!.clearProp(args[1].asString())
            is VObj -> {
                // TODO: clear prop on obj
            }
            else -> fail(E_TYPE, "cannot clearProp on ${args[0].type}")
        }
        return VVoid
    }

    private fun requireArgTypes(args: List<Value>, vararg argTypes: Value.Type?) {
        if (args.size != argTypes.size) fail(E_INVARG, "${argTypes.size} args required but ${args.size} provided")
        args.forEachIndexed { n, arg ->
            if (argTypes[n] != null && arg.type != argTypes[n]) fail(E_TYPE, "arg $n is ${arg.type} not ${argTypes[n]}")
        }
    }

    companion object {
        private const val TAG = "sys"

        private val CP437_TO_UTF8_TABLE = listOf(0, 9786, 9787, 9829, 9830, 9827, 9824, 8226, 9688, 9675, 9689, 9794, 9792, 9834, 9835,
            9788, 9658, 9668, 8597, 8252, 182, 167, 9644, 8616, 8593, 8595, 8594, 8592, 8735, 8596, 9650, 9660, 32, 33, 34, 35, 36, 37, 38,
            39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70,
            71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102,
            103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126,
            8962, 199, 252, 233, 226, 228, 224, 229, 231, 234, 235, 232, 239, 238, 236, 196, 197, 201, 230, 198, 244, 246, 242, 251,
            249, 255, 214, 220, 162, 163, 165, 8359, 402, 225, 237, 243, 250, 241, 209, 170, 186, 191, 8976, 172, 189, 188, 161, 171,
            187, 9617, 9618, 9619, 9474, 9508, 9569, 9570, 9558, 9557, 9571, 9553, 9559, 9565, 9564, 9563, 9488, 9492, 9524, 9516, 9500, 9472,
            9532, 9566, 9567, 9562, 9556, 9577, 9574, 9568, 9552, 9580, 9575, 9576, 9572, 9573, 9561, 9560, 9554, 9555, 9579, 9578, 9496, 9484,
            9608, 9604, 9612, 9616, 9600, 945, 223, 915, 960, 931, 963, 181, 964, 934, 920, 937, 948, 8734, 966, 949, 8745, 8801, 177, 8805,
            8804, 8992, 8993, 247, 8776, 176, 8729, 183, 8730, 8319, 178, 9632, 160)
    }

}
