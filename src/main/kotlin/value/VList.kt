package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException.Type.*

data class VList(var v: MutableList<Value>): Value() {

    override val type = Type.LIST

    override fun toString() = "[${v.joinToString(", ")}]"
    override fun asString() = v.joinToString(", ")

    override fun iterableSize() = v.size

    override fun getProp(c: Context, name: String): Value? {
        when (name) {
            "length" -> return propSize()
            "isEmpty" -> return propIsEmpty()
            "isNotEmpty" -> return propIsNotEmpty()
        }
        return null
    }

    override fun getIndex(c: Context, index: Value): Value? {
        if (index is VInt) {
            if (index.v < 0 || index.v >= v.size) fail(E_RANGE, "list index ${index.v} out of bounds")
            return v[index.v]
        }
        return null
    }

    override fun getRange(c: Context, from: Value, to: Value): Value? {
        if (from is VInt && to is VInt) {
            if (from.v < 0 || to.v >= v.size) fail(E_RANGE, "list range ${from.v}..${to.v} out of bounds")
            return VList(v.subList(from.v, to.v))
        }
        return null
    }

    override fun setIndex(c: Context, index: Value, value: Value): Boolean {
        if (index is VInt) {
            if (index.v < 0 || index.v >= v.size) fail(E_RANGE, "list index ${index.v} out of bounds")
            v[index.v] = value
            return true
        }
        return false
    }

    override fun setRange(c: Context, from: Value, to: Value, value: Value): Boolean {
        if (from is VInt && to is VInt) {
            if (from.v < 0 || to.v >= v.size) fail(E_RANGE, "list range ${from.v}..${to.v} out of bounds")
            if (from.v > to.v) fail(E_RANGE, "list range start after end")
            if (value !is VList) fail(E_TYPE, "list range insert must be a list")
            val old = v
            v = mutableListOf<Value>().apply {
                addAll(old.subList(0, from.v))
                addAll((value as VList).v)
                addAll(old.subList(to.v+1, old.size))
            }
            return true
        }
        return false
    }

    override fun callFunc(c: Context, name: String, args: List<Value>): Value? {
        when (name) {
            "join" -> return funcJoin(args)
        }
        return null
    }


    // Custom props

    private fun propSize(): Value = VInt(v.size)
    private fun propIsEmpty(): Value = VBool(v.isEmpty())
    private fun propIsNotEmpty(): Value = VBool(v.isNotEmpty())

    // Custom funcs

    private fun funcJoin(args: List<Value>): Value {
        if (args.size > 1) fail(E_RANGE, "incorrect number of arguments")
        return VString(v.joinToString(args[0].asString()) { it.asString() })
    }
}
