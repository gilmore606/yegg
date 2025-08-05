package com.dlfsystems.yegg.compiler

import com.dlfsystems.yegg.value.VInt
import com.dlfsystems.yegg.value.VNull
import com.dlfsystems.yegg.value.Value


// A value type specifier.

data class TypeSpec(
    // Index into Value.Type.entries (null for ANY).
    val i: Int? = null,
    // Is null allowed instead of the type?
    val nullable: Boolean? = null,
) {

    override fun toString() = i?.let {
        Value.Type.entries[it].toString() + if (nullable == true) "?" else ""
    } ?: "ANY"

    // Does the given value match this type?
    fun matches(v: Value) =
        (i == null) ||
        (nullable == true && v == VNull) ||
        (v.type == Value.Type.entries[i])

    // Encode to a VInt for inclusion in VM code.
    fun toVInt() = VInt(
        (i ?: -1) +
        (if (nullable == true) NULLABLE_MULT else 0)
    )


    companion object {
        private const val NULLABLE_MULT = 1000

        // Decode from a VInt at runtime.
        fun fromVInt(vi: VInt): TypeSpec {
            if (vi.v == -1) return TypeSpec()
            return if (vi.v >= NULLABLE_MULT)
                TypeSpec(vi.v - NULLABLE_MULT, true)
            else
                TypeSpec(vi.v, false)
        }
    }

}
