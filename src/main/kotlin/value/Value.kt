package com.dlfsystems.value

import com.dlfsystems.vm.Context
import java.util.UUID

// A literal value in VM language.

sealed class Value {
    enum class Type { VOID, BOOL, INT, FLOAT, STRING, THING, TRAIT }
    abstract val type: Type

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
    open fun plus(a2: Value): Value? = null
    open fun multiply(a2: Value): Value? = null
    open fun divide(a2: Value): Value? = null

    // Getting or setting a prop on this type.  Null raises E_PROPNF.
    open fun getProp(context: Context?, propname: String): Value? = null
    open fun setProp(context: Context?, propname: String, value: Value): Boolean = false

}
