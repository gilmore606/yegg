package com.dlfsystems.yegg.value

import com.dlfsystems.yegg.util.fail
import com.dlfsystems.yegg.vm.Context
import com.dlfsystems.yegg.vm.VMException.Type.E_INVARG
import com.dlfsystems.yegg.world.trait.Verb
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// A literal value in VM language.

@Serializable
sealed class Value {
    enum class Type { VOID, NULL, BOOL, INT, FLOAT, STRING, LIST, MAP, OBJ, TRAIT, FUN, TASK, ERR }

    @SerialName("yType")
    abstract val type: Type

    // String equivalent when added to a string.
    open fun asString(): String = toString()

    // Is this value considered true/false/zero in code?
    open fun isTrue(): Boolean = false
    fun isFalse(): Boolean = !isTrue()
    open fun isZero(): Boolean = false

    // How many elements do I have for iteration?
    open fun iterableSize(): Int? = null

    // Comparisons between this type and any other value.
    open fun cmpEq(a2: Value): Boolean = false
    open fun cmpGt(a2: Value): Boolean = false
    open fun cmpGe(a2: Value): Boolean = false
    fun cmpLt(a2: Value): Boolean = !cmpGe(a2)
    fun cmpLe(a2: Value): Boolean = !cmpGt(a2)

    // Boolean 'this in a2'.  Null raises E_TYPE.
    open fun isIn(a2: Value): Boolean? = a2.contains(this)
    open fun contains(a2: Value): Boolean? = null

    // Math between this type and any other value.  Null raises E_TYPE.
    open fun negate(): Value? = null
    open fun plus(a2: Value): Value? = null
    open fun multiply(a2: Value): Value? = null
    open fun divide(a2: Value): Value? = null
    open fun toPower(a2: Value): Value? = null
    open fun modulo(a2: Value): Value? = null

    // Get or set a prop on this type.  Null raises E_PROPNF.
    open fun getProp(name: String): Value? = null
    open fun setProp(name: String, value: Value): Boolean = false

    // Get or set an index/range on this type.  Null raises E_TYPE.
    open fun getIndex(index: Value): Value? = null
    open fun getRange(from: Value, to: Value): Value? = null
    open fun setIndex(index: Value, value: Value): Boolean = false
    open fun setRange(from: Value, to: Value, value: Value): Boolean = false

    // Call a static built-in verb on this type and return its value.
    // Return null if no such static verb.
    open fun callStaticVerb(c: Context, name: String, args: List<Value>): Value? = null

    // Get a verb on this type.  Return null if no such verb.
    open fun getVerb(name: String): Verb? = null

    protected fun requireArgCount(args: List<Value>, min: Int, max: Int) {
        if (args.size < min || args.size > max) fail(E_INVARG, "incorrect number of args")
    }

}
