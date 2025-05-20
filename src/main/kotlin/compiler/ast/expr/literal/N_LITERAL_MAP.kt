package com.dlfsystems.compiler.ast.expr.literal

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.expr.N_EXPR
import com.dlfsystems.value.VMap
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Opcode.O_MAPVAL

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
    override fun code(coder: Coder) {
        if (codeConstant(coder)) return
        value.keys.forEach { key ->
            value[key]!!.code(coder)
            key.code(coder)
        }
        coder.code(this, O_MAPVAL)
        coder.value(this, value.size)
    }
}
