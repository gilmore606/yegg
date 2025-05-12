package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.value.*
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.world.ObjID

// A literal value appearing in code.

abstract class N_LITERAL: N_EXPR() {
    open fun codeValue(coder: Coder) { }

    override fun code(coder: Coder) {
        coder.code(this, O_VAL)
        codeValue(coder)
    }
}

class N_LITERAL_BOOLEAN(val value: Boolean): N_LITERAL() {
    override fun toText() = if (value) "true" else "false"
    override fun codeValue(coder: Coder) { coder.value(this, value) }
    override fun constantValue() = VBool(value)
}

class N_LITERAL_INTEGER(val value: Int): N_LITERAL() {
    override fun toText() = "$value"
    override fun codeValue(coder: Coder) { coder.value(this, value) }
    override fun constantValue() = VInt(value)
}

class N_LITERAL_FLOAT(val value: Float): N_LITERAL() {
    override fun toText() = "$value"
    override fun codeValue(coder: Coder) { coder.value(this, value) }
    override fun constantValue() = VFloat(value)
}

class N_LITERAL_OBJ(val objID: ObjID): N_LITERAL() {
    override fun toText() = "#$id"
    override fun codeValue(coder: Coder) { coder.value(this, objID) }
    override fun constantValue() = VObj(objID)
}

class N_LITERAL_STRING(val value: String): N_LITERAL() {
    override fun toText() = "\"$value\""
    override fun codeValue(coder: Coder) { coder.value(this, value) }
    override fun constantValue() = VString(value)
}

class N_LITERAL_LIST(val value: List<N_EXPR>): N_LITERAL() {
    override fun kids() = value
    override fun toText() = value.joinToString(", ", "LIST[", "]")
    override fun constantValue(): Value? {
        val constant = mutableListOf<Value>()
        for (expr in value) {
            val exprConstant = expr.constantValue()
            if (exprConstant == null) return null else constant.add(exprConstant)
        }
        return VList(constant)
    }
    override fun code(coder: Coder) {
        if (codeConstant(coder)) return
        value.forEach { it.code(coder) }
        coder.code(this, O_LISTVAL)
        coder.value(this, value.size)
    }
}

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

class N_LITERAL_FUN(val args: List<N_IDENTIFIER>, val block: N_STATEMENT): N_LITERAL() {
    override fun kids() = args + listOf(block)
    override fun code(coder: Coder) {
        coder.code(this, O_VAL)
        coder.value(this, args.map { VString(it.name) })
        coder.code(this, O_VAL)
        coder.value(this, block.collectVars().map { VString(it) })
        coder.code(this, O_FUNVAL)
        val blockID = coder.codeBlockStart(this)
        coder.code(this, O_JUMP)
        coder.jumpForward(this, "skipFun")
        block.code(coder)
        coder.code(this, O_RETURN)
        coder.codeBlockEnd(this, blockID)
        coder.setForwardJump(this, "skipFun")
    }
}
