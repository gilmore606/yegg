package com.dlfsystems.world.trait

import com.dlfsystems.value.VInt
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context

// A special trait which exists in every world.
// Provides access to system environment properties, metadata about the world, and server control.

class SysTrait : Trait("sys") {

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "time" -> return VInt((System.currentTimeMillis() / 1000L).toInt())
            "tickLimit" -> return VInt(100000) // TODO: replace with actual props
            "stackLimit" -> return VInt(1000)
        }
        return super.getProp(c, name)
    }

    override fun callFunc(c: Context, name: String): Value? {
        when (name) {

        }
        return super.callFunc(c, name)
    }

}
