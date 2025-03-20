package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException.Type.*

data class VList(var v: MutableList<Value>): Value() {

    override val type = Type.LIST

    override fun toString() = "[${v.joinToString(", ")}]"
    override fun asString() = v.joinToString(", ")

    override fun iterableSize() = v.size

    override fun contains(a2: Value) = v.contains(a2)

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
            "push" -> return funcPush(args)
            "pop" -> return funcPop(args)
            "contains" -> return funcContains(args)
            "indexOf" -> return funcIndexOf(args)
        }
        return null
    }


    // Custom props

    private fun propSize(): Value = VInt(v.size)
    private fun propIsEmpty(): Value = VBool(v.isEmpty())
    private fun propIsNotEmpty(): Value = VBool(v.isNotEmpty())

    // Custom funcs

    private fun funcJoin(args: List<Value>): Value {
        requireArgCount(args, 0, 1)
        return VString(
            v.joinToString(
                if (args.isEmpty()) " " else args[0].asString()
            ) { it.asString() }
        )
    }

    private fun funcPush(args: List<Value>): Value {
        requireArgCount(args, 1, 1)
        v.add(0, args[0])
        return VVoid()
    }

    private fun funcPop(args: List<Value>): Value {
        requireArgCount(args, 0, 0)
        if (v.isEmpty()) fail(E_RANGE, "cannot pop empty list")
        return v.removeAt(0)
    }

    private fun funcContains(args: List<Value>): Value {
        requireArgCount(args, 1, 1)
        return VBool(v.contains(args[0]))
    }

    private fun funcIndexOf(args: List<Value>): Value {
        requireArgCount(args, 1, 1)
        return if (v.contains(args[0])) VInt(v.indexOf(args[0])) else VInt(-1)
    }
}
