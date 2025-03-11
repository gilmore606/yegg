package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*

abstract class N_EXPR: N_STATEMENT()

class N_IFELSE(val condition: N_EXPR, val eThen: N_STATEMENT, val eElse: N_STATEMENT? = null): N_EXPR() {
    override fun toText() = eElse?.let { "(if $condition $eThen else $eElse)" } ?: "if $condition $eThen"
    override fun kids() = mutableListOf(condition, eThen).apply { eElse?.also { add(it) }}
}

abstract class N_MATH_BINOP(val opString: String, val left: N_EXPR, val right: N_EXPR): N_EXPR() {
    override fun toText() = "($left $opString $right)"
    override fun kids() = listOf(left, right)
}
class N_ADD(left: N_EXPR, right: N_EXPR): N_MATH_BINOP("+", left, right) {
    override fun code(coder: Coder) {
        left.code(coder)
        right.code(coder)
        coder.code(this, O_ADD)
    }
}
class N_SUBTRACT(left: N_EXPR, right: N_EXPR): N_MATH_BINOP("-", left, right) {
    override fun code(coder: Coder) {
        left.code(coder)
        right.code(coder)
        coder.code(this, O_NEGATE)
        coder.code(this, O_ADD)
    }
}
class N_MULTIPLY(left: N_EXPR, right: N_EXPR): N_MATH_BINOP("*", left, right) {
    override fun code(coder: Coder) {
        left.code(coder)
        right.code(coder)
        coder.code(this, O_MULT)
    }
}
class N_DIVIDE(left: N_EXPR, right: N_EXPR): N_MATH_BINOP("/", left, right) {
    override fun code(coder: Coder) {
        left.code(coder)
        right.code(coder)
        coder.code(this, O_DIV)
    }
}
class N_POWER(left: N_EXPR, right: N_EXPR): N_MATH_BINOP("^", left, right) {
    override fun code(coder: Coder) {
        left.code(coder)
        right.code(coder)
        coder.code(this, O_POWER)
    }
}
class N_MODULUS(left: N_EXPR, right: N_EXPR): N_MATH_BINOP("%", left, right) {
    override fun code(coder: Coder) {
        left.code(coder)
        right.code(coder)
        coder.code(this, O_MODULUS)
    }
}

abstract class N_MATH_UNOP(val opString: String, val expr: N_EXPR): N_EXPR() {
    override fun toText() = "$opString$expr"
    override fun kids() = listOf(expr)
}
class N_INVERSE(expr: N_EXPR): N_MATH_UNOP("!", expr)
class N_NEGATE(expr: N_EXPR): N_MATH_UNOP("-", expr) {
    override fun code(coder: Coder) {
        expr.code(coder)
        coder.code(this, O_NEGATE)
    }
}

abstract class N_LOGIC_BINOP(val opString: String, val left: N_EXPR, val right: N_EXPR): N_EXPR() {
    override fun toText() = "($left $opString $right)"
    override fun kids() = listOf(left, right)
}
class N_LOGIC_AND(left: N_EXPR, right: N_EXPR): N_LOGIC_BINOP("&&", left, right)
class N_LOGIC_OR(left: N_EXPR, right: N_EXPR): N_LOGIC_BINOP("||", left, right)

class N_CONDITIONAL(val condition: N_EXPR, val eTrue: N_EXPR, val eFalse: N_EXPR): N_EXPR() {
    override fun toText() = "($condition ? $eTrue : $eFalse)"
    override fun kids() = listOf(condition, eTrue, eFalse)
}

abstract class N_COMPARE_BINOP(val opString: String, val left: N_EXPR, val right: N_EXPR): N_EXPR() {
    override fun toText() = "($left $opString $right)"
    override fun kids() = listOf(left, right)
}
class N_EQUALS(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP("==", left, right)
class N_NOTEQUALS(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP("!=", left, right)
class N_GREATER_THAN(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP(">", left, right)
class N_LESS_THAN(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP("<", left, right)
class N_GREATER_EQUAL(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP(">=", left, right)
class N_LESS_EQUAL(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP("<=", left, right)

class N_INDEXREF(val left: N_EXPR, val index: N_EXPR): N_EXPR() {
    override fun toText() = "$left[$index]"
    override fun kids() = listOf(left, index)
}

class N_DOTREF(val left: N_EXPR, val right: N_EXPR): N_EXPR() {
    override fun toText() = "$left.$right"
    override fun kids() = listOf(left, right)
}

class N_FUNCALL(val left: N_EXPR, val args: List<N_EXPR>): N_EXPR() {
    override fun toText() = "$left($args)"
    override fun kids() = mutableListOf(left).apply { addAll(args) }
}
