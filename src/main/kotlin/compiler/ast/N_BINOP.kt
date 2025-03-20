package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode
import com.dlfsystems.vm.Opcode.*

// A binary operation, popping two stack values and pushing one result.

abstract class N_BINOP(val opString: String, val left: N_EXPR, val right: N_EXPR, val ops: List<Opcode>): N_EXPR() {
    override fun toText() = "($left $opString $right)"
    override fun kids() = listOf(left, right)

    override fun code(coder: Coder) {
        left.code(coder)
        right.code(coder)
        ops.forEach { coder.code(this, it) }
    }
}

class N_ADD(left: N_EXPR, right: N_EXPR): N_BINOP("+", left, right, listOf(O_ADD))
class N_SUBTRACT(left: N_EXPR, right: N_EXPR): N_BINOP("-", left, right, listOf(O_NEGATE, O_ADD))
class N_MULTIPLY(left: N_EXPR, right: N_EXPR): N_BINOP("*", left, right, listOf(O_MULT))
class N_DIVIDE(left: N_EXPR, right: N_EXPR): N_BINOP("/", left, right, listOf(O_DIV))
class N_POWER(left: N_EXPR, right: N_EXPR): N_BINOP("^", left, right, listOf(O_POWER))
class N_MODULUS(left: N_EXPR, right: N_EXPR): N_BINOP("%", left, right, listOf(O_MODULUS))
class N_AND(left: N_EXPR, right: N_EXPR): N_BINOP("&&", left, right, listOf(O_AND))
class N_OR(left: N_EXPR, right: N_EXPR): N_BINOP("||", left, right, listOf(O_OR))
class N_IN(left: N_EXPR, right: N_EXPR): N_BINOP("in", left, right, listOf(O_IN))

class N_CMP_EQ(left: N_EXPR, right: N_EXPR): N_BINOP("==", left, right, listOf(O_CMP_EQ))
class N_CMP_NEQ(left: N_EXPR, right: N_EXPR): N_BINOP("!=", left, right, listOf(O_CMP_EQ, O_NEGATE))
class N_CMP_GT(left: N_EXPR, right: N_EXPR): N_BINOP(">", left, right, listOf(O_CMP_GT))
class N_CMP_LT(left: N_EXPR, right: N_EXPR): N_BINOP("<", left, right, listOf(O_CMP_LT))
class N_CMP_GE(left: N_EXPR, right: N_EXPR): N_BINOP(">=", left, right, listOf(O_CMP_GE))
class N_CMP_LE(left: N_EXPR, right: N_EXPR): N_BINOP("<=", left, right, listOf(O_CMP_LE))
