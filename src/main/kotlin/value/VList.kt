package com.dlfsystems.yegg.value

import com.dlfsystems.yegg.server.Yegg
import com.dlfsystems.yegg.util.fail
import com.dlfsystems.yegg.vm.Context
import com.dlfsystems.yegg.vm.VMException.Type.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VList")
data class VList(var v: MutableList<Value> = mutableListOf()): Value() {
    override fun equals(other: Any?) = other is VList && v == other.v
    override fun hashCode() = javaClass.hashCode()

    @SerialName("yType")
    override val type = Type.LIST

    override fun toString() = "[${v.joinToString(", ")}]"
    override fun asString() = v.joinToString(", ")

    override fun iterableSize() = v.size

    override fun contains(a2: Value) = v.contains(a2)

    override fun plus(a2: Value) = when (a2) {
        is VList -> VList(v.toMutableList().apply { addAll(a2.v) })
        else -> null
    }

    override fun getProp(name: String) = when (name) {
        "size" -> VInt(v.size)
        "isEmpty" -> if (v.isEmpty()) Yegg.vTrue else Yegg.vFalse
        "isNotEmpty" -> if (v.isEmpty()) Yegg.vFalse else Yegg.vTrue
        "first" -> if (v.isEmpty()) VVoid else v.first()
        "last" -> if (v.isEmpty()) VVoid else v.last()
        "random" -> if (v.isEmpty()) VVoid else v.random()
        "reversed" -> make(v.reversed())
        "shuffled" -> make(v.shuffled())
        "sorted" -> propSorted()
        else -> null
    }

    private fun propSorted(): VList {
        if (v.isEmpty()) return make(v)
        return make(when (v[0]) {
            is VInt -> v.sortedBy { (it as VInt).v }
            is VFloat -> v.sortedBy { (it as VFloat).v }
            else -> v.sortedBy { it.asString() }
        })
    }

    override fun getIndex(index: Value): Value? {
        if (index is VInt) {
            if (index.v < 0 || index.v >= v.size) fail(E_RANGE, "list index ${index.v} out of bounds")
            return v[index.v]
        }
        return null
    }

    override fun getRange(from: Value, to: Value): Value? {
        if (from is VInt && to is VInt) {
            if (from.v < 0 || to.v >= v.size) fail(E_RANGE, "list range ${from.v}..${to.v} out of bounds")
            return VList(v.subList(from.v, to.v + 1))
        }
        return null
    }

    override fun setIndex(index: Value, value: Value): Boolean {
        if (index is VInt) {
            if (index.v < 0 || index.v >= v.size) fail(E_RANGE, "list index ${index.v} out of bounds")
            v[index.v] = value
            return true
        }
        return false
    }

    override fun setRange(from: Value, to: Value, value: Value): Boolean {
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

    override fun callStaticVerb(c: Context, name: String, args: List<Value>) = when (name) {
        "join" -> verbJoin(args)
        "push" -> verbPush(args)
        "pop" -> verbPop(args)
        "deque" -> verbDeque(args)
        "contains" -> verbContains(args)
        "indexOf" -> verbIndexOf(args)
        "lastIndexOf" -> verbLastIndexOf(args)
        "add" -> verbAdd(args)
        "addAll" -> verbAddAll(args)
        "setAdd" -> verbSetAdd(args)
        "setAddAll" -> verbSetAddAll(args)
        "remove" -> verbRemove(args)
        "removeAt" -> verbRemoveAt(args)
        "removeAll" -> verbRemoveAll(args)
        "clear" -> verbClear(args)
        "reverse" -> verbReverse(args)
        "shuffle" -> verbShuffle(args)
        else -> null
    }

    private fun verbJoin(args: List<Value>): VString {
        requireArgCount(args, 0, 1)
        return VString(
            v.joinToString(
                if (args.isEmpty()) " " else args[0].asString()
            ) { it.asString() }
        )
    }

    private fun verbPush(args: List<Value>): VVoid {
        requireArgCount(args, 1, 1)
        v.add(0, args[0])
        return VVoid
    }

    private fun verbPop(args: List<Value>): Value {
        requireArgCount(args, 0, 0)
        if (v.isEmpty()) fail(E_RANGE, "cannot pop empty list")
        return v.removeAt(0)
    }

    private fun verbDeque(args: List<Value>): Value {
        requireArgCount(args, 0, 0)
        if (v.isEmpty()) fail(E_RANGE, "cannot deque empty list")
        return v.removeAt(v.lastIndex)
    }

    private fun verbContains(args: List<Value>): VBool {
        requireArgCount(args, 1, 1)
        return VBool(v.contains(args[0]))
    }

    private fun verbIndexOf(args: List<Value>): VInt {
        requireArgCount(args, 1, 1)
        return if (v.contains(args[0])) VInt(v.indexOf(args[0])) else VInt(-1)
    }

    private fun verbLastIndexOf(args: List<Value>): VInt {
        requireArgCount(args, 1, 1)
        return if (v.contains(args[0])) VInt(v.lastIndexOf(args[0])) else VInt(-1)
    }

    private fun verbAdd(args: List<Value>): VVoid {
        requireArgCount(args, 1, 2)
        if (args.size == 1) {
            v.add(args[0])
            return VVoid
        }
        val pos = positionArg(args[0])
        v.add(pos, args[1])
        return VVoid
    }

    private fun verbAddAll(args: List<Value>): VVoid {
        requireArgCount(args, 1, 1)
        if (args[0].type != Type.LIST) fail(E_TYPE, "${args[0].type} is not LIST")
        v.addAll((args[0] as VList).v)
        return VVoid
    }

    private fun verbSetAdd(args: List<Value>): VVoid {
        requireArgCount(args, 1, 1)
        if (!v.contains(args[0])) v.add(args[0])
        return VVoid
    }

    private fun verbSetAddAll(args: List<Value>): VVoid {
        requireArgCount(args, 1, 1)
        if (args[0].type != Type.LIST) fail(E_TYPE, "${args[0].type} is not LIST")
        (args[0] as VList).v.forEach { if (!v.contains(it)) v.add(it) }
        return VVoid
    }

    private fun verbRemove(args: List<Value>): VVoid {
        requireArgCount(args, 1, 1)
        v.remove(args[0])
        return VVoid
    }

    private fun verbRemoveAt(args: List<Value>): Value {
        requireArgCount(args, 1, 1)
        val pos = positionArg(args[0])
        return v.removeAt(pos)
    }

    private fun verbRemoveAll(args: List<Value>): VVoid {
        requireArgCount(args, 1, 1)
        if (args[0].type != Type.LIST) fail(E_TYPE, "${args[0].type} is not LIST")
        v.removeAll((args[0] as VList).v)
        return VVoid
    }

    private fun verbClear(args: List<Value>): VVoid {
        requireArgCount(args, 0, 0)
        v.clear()
        return VVoid
    }

    private fun verbReverse(args: List<Value>): VVoid {
        requireArgCount(args, 0, 0)
        v.reverse()
        return VVoid
    }

    private fun verbShuffle(args: List<Value>): VVoid {
        requireArgCount(args, 0, 0)
        v.shuffle()
        return VVoid
    }

    private fun positionArg(arg: Value): Int {
        if (arg.type != Type.INT) fail(E_TYPE, "invalid ${arg.type} list position")
        val pos = (arg as VInt).v
        if (pos < 0 || pos >= v.size) fail(E_RANGE, "$pos out of bounds 0..${v.lastIndex}")
        return pos
    }

    companion object {
        fun make(v: List<Value>) = VList(v.toMutableList())
    }
}
