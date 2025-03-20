package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException
import com.dlfsystems.vm.VMException.Type.*

data class VMap(val v: MutableMap<String, Value>): Value() {

    var realKeys = mutableMapOf<String, Value>()

    override val type = Type.MAP
    override fun toString() = "[${v.entries.joinToString()}]"
    override fun asString() = v.entries.joinToString(", ")

    override fun contains(a2: Value) = realKeys.values.contains(a2)

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "length" -> return propLength()
            "keys" -> return propKeys()
            "values" -> return propValues()
        }
        return null
    }

    override fun getIndex(c: Context, index: Value): Value? {
        index.asMapKey()?.also {
            if (v.containsKey(it)) return v[it]
            else fail(E_RANGE, "no map entry $index")
        }
        return null
    }

    override fun setIndex(c: Context, index: Value, value: Value): Boolean {
        index.asMapKey()?.also {
            realKeys[it] = index
            v[it] = value
        }
        return false
    }

    override fun callFunc(c: Context, name: String, args: List<Value>): Value? {
        when (name) {
            "containsKey" -> return funcContainsKey(args)
            "containsValue" -> return funcContainsValue(args)
        }
        return null
    }

    // We make new VMaps statically so we can use a constructed string as the map key,
    // instead of the Value object itself.  We save the original key so it can be
    // returned by this.keys.
    companion object {
        fun make(v: Map<Value, Value>): VMap {
            val reals = mutableMapOf<String, Value>()
            val map = mutableMapOf<String, Value>()
            v.keys.forEach { key ->
                key.asMapKey()?.also {
                    reals.put(it, key)
                    map.put(it, v[key]!!)
                } ?: throw VMException(E_TYPE, "${key.type} cannot be map key", 0, 0) // TODO: get real line+char
            }
            return VMap(map).apply { realKeys = reals }
        }
    }


    // Custom props

    private fun propLength() = VInt(v.size)
    private fun propKeys() = VList(realKeys.values.toMutableList())
    private fun propValues() = VList(v.values.toMutableList())

    // Custom funcs

    private fun funcContainsKey(args: List<Value>): Value {
        requireArgCount(args, 1, 1)
        return VBool(realKeys.containsValue(args[0]))
    }

    private fun funcContainsValue(args: List<Value>): Value {
        requireArgCount(args, 1, 1)
        return VBool(v.containsValue(args[0]))
    }
}
