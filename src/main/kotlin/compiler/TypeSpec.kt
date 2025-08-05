package com.dlfsystems.yegg.compiler

import com.dlfsystems.yegg.value.VInt
import com.dlfsystems.yegg.value.VNull
import com.dlfsystems.yegg.value.Value


// A value type specifier.

data class TypeSpec(
    val t: Value.Type? = null,
    val nullable: Boolean? = null,
) {

    override fun toString() = t?.let {
        it.toString() + if (nullable == true) "?" else ""
    } ?: "ANY"

    // Does the given value match this type?
    fun matches(v: Value) =
        (t == null) ||
        (nullable == true && v == VNull) ||
        (v.type == t)

    // Encode to a VInt for inclusion in VM code.
    fun toVInt() = VInt(
        (t?.let { Value.Type.entries.indexOf(it) } ?: -1) +
        (if (nullable == true) NULLABLE_MULT else 0)
    )


    companion object {
        private const val NULLABLE_MULT = 1000

        // Decode from a VInt at runtime.
        fun fromVInt(vi: VInt) =
            if (vi.v == -1)
                TypeSpec()
            else if (vi.v >= NULLABLE_MULT)
                TypeSpec(Value.Type.entries[vi.v - NULLABLE_MULT], true)
            else
                TypeSpec(Value.Type.entries[vi.v], false)
    }

}
