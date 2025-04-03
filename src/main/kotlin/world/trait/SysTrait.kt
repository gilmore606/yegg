package com.dlfsystems.world.trait

import com.dlfsystems.Yegg
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

    override fun getProp(c: Context, propName: String): Value? {
        when (propName) {
            "time" -> return VInt((System.currentTimeMillis() / 1000L).toInt())
        }
        return super.getProp(c, propName)
    }

    override fun callVerb(c: Context, verbName: String, args: List<Value>): Value? {
        when (verbName) {
            "addTrait" -> return verbAddTrait(c, args)
            "create" -> return verbCreate(c, args)
            "dumpDatabase" -> return verbDumpDatabase(c, args)
        }
        return super.callVerb(c, verbName, args)
    }

    private fun verbAddTrait(c: Context, args: List<Value>): Value {
        if (args.size != 1 || args[0] !is VString) throw IllegalArgumentException("Bad args for addTrait")
        c.world.addTrait(args[0].asString())
        return VVoid()
    }

    private fun verbCreate(c: Context, args: List<Value>): Value {
        val obj = c.world.createObj()
        try {
            args.forEach {
                if (it !is VTrait) throw IllegalArgumentException("Non-trait passed to create")
                c.world.applyTrait(it.v!!, obj.id)
            }
            return VObj(obj.id)
        } catch (e: Exception) {
            c.world.recycleObj(obj.id)
            throw e
        }
    }

    private fun verbDumpDatabase(c: Context, args: List<Value>): Value {
        if (args.isNotEmpty()) throw IllegalArgumentException("Bad args for dumpDatabase")
        Yegg.dumpDatabase()?.also { return VString(it) }
        return VVoid()
    }

}
