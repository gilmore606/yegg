package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

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
}

class N_LITERAL_INTEGER(val value: Int): N_LITERAL() {
    override fun toText() = "$value"
    override fun codeValue(coder: Coder) { coder.value(this, value) }
}

class N_LITERAL_FLOAT(val value: Float): N_LITERAL() {
    override fun toText() = "$value"
    override fun codeValue(coder: Coder) { coder.value(this, value) }
}

class N_LITERAL_STRING(val value: String): N_LITERAL() {
    override fun toText() = "\"$value\""
    override fun codeValue(coder: Coder) { coder.value(this, value) }
}

class N_LITERAL_LIST(val value: List<N_EXPR>): N_LITERAL() {
    override fun kids() = value
    override fun toText() = value.joinToString(", ", "LIST[", "]")
    override fun code(coder: Coder) {
        value.forEach { it.code(coder) }
        coder.code(this, O_LISTVAL)
        coder.value(this, value.size)
    }
}

class N_LITERAL_MAP(val value: Map<N_EXPR, N_EXPR>): N_LITERAL() {
    override fun kids() = (value.keys + value.values).toList()
    override fun toText() = value.keys.joinToString(", ", "MAP[", "]") { "$it: ${value[it]}" }
    override fun code(coder: Coder) {
        value.keys.forEach { key ->
            value[key]!!.code(coder)
            key.code(coder)
        }
        coder.code(this, O_MAPVAL)
        coder.value(this, value.size)
    }
}

class N_LITERAL_FUN(val args: List<N_IDENTIFIER>, val block: N_BLOCK): N_LITERAL() {
    override fun kids() = listOf(block)
    override fun code(coder: Coder) {
        args.forEach {
            coder.code(this, O_VAL)
            coder.value(this, it.name)
        }
        val vars = block.collectVars()
        vars.forEach {
            coder.code(this, O_VAL)
            coder.value(this, it)
        }
        coder.code(this, O_FUNVAL)
        coder.value(this, args.size)
        coder.value(this, vars.size)
        coder.codeEntryPoint(this)
        coder.code(this, O_JUMP)
        coder.jumpForward(this, "skipFun$id")
        block.code(coder)
        coder.code(this, O_RETURN)
        coder.setForwardJump(this, "skipFun$id")
    }
}
