package com.dlfsystems.value

import com.dlfsystems.vm.Context
import com.dlfsystems.vm.VMException

// A literal value in VM language.

sealed class Value {
    enum class Type { VOID, BOOL, INT, FLOAT, STRING, LIST, MAP, OBJ, TRAIT }
    abstract val type: Type

    fun fail(type: VMException.Type, m: String) { throw VMException(type, m, 0, 0) } // TODO: get line+char here somehow

    // String equivalent for use as a map key.  Null if this value can't be a map key.
    open fun asMapKey(): String? = null
    // String equivalent when added to a string.
    open fun asString(): String = "VALUE"

    // Is this value considered true/false/zero in code?
    open fun isTrue(): Boolean = false
    fun isFalse(): Boolean = !isTrue()
    open fun isZero(): Boolean = false

    // Comparisons between this type and any other value.
    open fun cmpEq(a2: Value): Boolean = false
    open fun cmpGt(a2: Value): Boolean = false
    open fun cmpGe(a2: Value): Boolean = false
    fun cmpLt(a2: Value): Boolean = !cmpGe(a2)
    fun cmpLe(a2: Value): Boolean = !cmpGt(a2)

    // Math between this type and any other value.  Null raises E_TYPE.
    open fun negate(): Value? = null
    open fun plus(a2: Value): Value? = null
    open fun multiply(a2: Value): Value? = null
    open fun divide(a2: Value): Value? = null

    // Getting or setting a prop on this type.  Null raises E_PROPNF.
    open fun getProp(c: Context, name: String): Value? = null
    open fun setProp(c: Context, name: String, value: Value): Boolean = false

    // Getting or setting an index/range on this type.  Null raises E_TYPE.
    open fun getIndex(c: Context, index: Value): Value? = null
    open fun getRange(c: Context, from: Value, to: Value): Value? = null
    open fun setIndex(c: Context, index: Value, value: Value): Boolean = false
    open fun setRange(c: Context, from: Value, to: Value, value: Value): Boolean = false
}
