package com.dlfsystems.yegg.compiler.ast.expr.literal

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.value.VMap
import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.vm.Opcode.O_MAPVAL

class N_LITERAL_MAP(val value: Map<N_EXPR, N_EXPR>): N_LITERAL() {
    override fun kids() = (value.keys + value.values).toList()
    override fun toText() = value.keys.joinToString(", ", "MAP[", "]") { "$it: ${value[it]}" }
    override fun constantValue(): Value? {
        val constant = mutableMapOf<Value, Value>()
        for (key in value.keys) {
            val keyConstant = key.constantValue()
            if (keyConstant == null) return null else {
                val valConstant = value[key]!!.constantValue()
                if (valConstant == null) return null else {
                    constant.set(keyConstant, valConstant)
                }
            }
        }
        return VMap(constant)
    }
    override fun code(c: Coder) {
        if (codeConstant(c)) return
        value.keys.forEach { key ->
            value[key]!!.code(c)
            key.code(c)
        }
        c.opcode(this, O_MAPVAL)
        c.value(this, value.size)
    }
}
