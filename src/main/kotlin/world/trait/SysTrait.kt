package com.dlfsystems.world.trait

import com.dlfsystems.value.VInt
import com.dlfsystems.value.VString
import com.dlfsystems.value.VVoid
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context

// A special trait which exists in every world.
// Provides access to system environment properties, metadata about the world, and server control.

class SysTrait : Trait("sys") {

    override val props = mutableMapOf<String, Value>(
        "tickLimit" to VInt(100000),
        "stackLimit" to VInt(100)
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
        }
        return super.callVerb(c, verbName, args)
    }

    private fun verbAddTrait(c: Context, args: List<Value>): Value {
        if (args.size != 1 || args[0] !is VString) throw IllegalArgumentException("Bad args for addTrait")
        c.world.addTrait(args[0].asString())
        return VVoid()
    }

}
