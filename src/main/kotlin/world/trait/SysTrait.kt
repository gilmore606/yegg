package com.dlfsystems.world.trait

import com.dlfsystems.server.Yegg
import com.dlfsystems.value.*
import com.dlfsystems.vm.Context

// A special trait which exists in every world.
// Provides access to system environment properties, metadata about the world, and server control.

class SysTrait : Trait("sys") {

    override val props = mutableMapOf<String, Value>(
        "tickLimit" to VInt(100000),
        "stackLimit" to VInt(100),
        "callLimit" to VInt(50),
    )

    override fun getProp(propName: String): Value? {
        when (propName) {
            "time" -> return VInt((System.currentTimeMillis() / 1000L).toInt())
        }
        return super.getProp(propName)
    }

    override fun callVerb(c: Context, verbName: String, args: List<Value>): Value? {
        when (verbName) {
            "addTrait" -> return verbAddTrait(args)
            "create" -> return verbCreate(args)
            "destroy" -> return verbDestroy(args)
            "move" -> return verbMove(args)
            "dumpDatabase" -> return verbDumpDatabase(args)
        }
        return super.callVerb(c, verbName, args)
    }

    private fun verbAddTrait(args: List<Value>): Value {
        if (args.size != 1 || args[0] !is VString) throw IllegalArgumentException("Bad args for addTrait")
        Yegg.world.addTrait(args[0].asString())
        return VVoid()
    }

    private fun verbCreate(args: List<Value>): Value {
        val obj = Yegg.world.createObj()
        try {
            args.forEach {
                if (it !is VTrait) throw IllegalArgumentException("Non-trait passed to create")
                Yegg.world.applyTrait(it.v!!, obj.id)
            }
            return VObj(obj.id)
        } catch (e: Exception) {
            Yegg.world.destroyObj(obj)
            throw e
        }
    }

    private fun verbDestroy(args: List<Value>): Value {
        if (args.size != 1 || args[0] !is VObj) throw IllegalArgumentException("Bad args for destroy")
        Yegg.world.getObj((args[0] as VObj).v)?.also { subject ->
            Yegg.world.destroyObj(subject)
        } ?: throw IllegalArgumentException("Cannot destroy invalid obj")
        return VVoid()
    }

    private fun verbMove(args: List<Value>): Value {
        if (args.size != 2 || args[0] !is VObj || args[1] !is VObj) throw IllegalArgumentException("Bad args for move")
        Yegg.world.getObj((args[0] as VObj).v)?.also { subject ->
            Yegg.world.moveObj(subject, args[1] as VObj)
        } ?: throw IllegalArgumentException("Cannot move invalid obj")
        return VVoid()
    }

    private fun verbDumpDatabase(args: List<Value>): Value {
        if (args.isNotEmpty()) throw IllegalArgumentException("Bad args for dumpDatabase")
        Yegg.dumpDatabase()?.also { return VString(it) }
        return VVoid()
    }

}
