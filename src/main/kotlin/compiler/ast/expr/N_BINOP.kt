package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.value.VVoid
import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.vm.Opcode
import com.dlfsystems.yegg.vm.Opcode.*

// A binary operation, popping two stack values and pushing one result.

abstract class N_BINOP(
    val opString: String,
    val left: N_EXPR,
    val right: N_EXPR,
    val ops: List<Opcode>
): N_EXPR() {

    override fun toString() = "($left $opString $right)"
    override fun kids() = listOf(left, right)
    override fun constantValue(): Value? {
        left.constantValue()?.also { leftConstant ->
            right.constantValue()?.also { rightConstant ->
                asConstant(leftConstant, rightConstant)?.also { return it }
            }
        }
        return null
    }
    open fun asConstant(l: Value, r: Value): Value? = null

    override fun code(c: Coder) = with (c.use(this)) {
        if (!codeConstant(c)) {
            code(left)
            code(right)
            ops.forEach { opcode(it) }
        }
    }

}


class N_ADD(left: N_EXPR, right: N_EXPR): N_BINOP("+", left, right, listOf(O_ADD)) {
    override fun asConstant(l: Value, r: Value) = l.plus(r)
}
class N_SUBTRACT(left: N_EXPR, right: N_EXPR): N_BINOP("-", left, right, listOf(O_NEGATE, O_ADD)) {
    override fun asConstant(l: Value, r: Value) = l.plus(r.negate() ?: VVoid)
}
class N_MULTIPLY(left: N_EXPR, right: N_EXPR): N_BINOP("*", left, right, listOf(O_MULT))  {
    override fun asConstant(l: Value, r: Value) = l.multiply(r)
}
class N_DIVIDE(left: N_EXPR, right: N_EXPR): N_BINOP("/", left, right, listOf(O_DIV)) {
    override fun asConstant(l: Value, r: Value) = l.divide(r)
}
class N_POWER(left: N_EXPR, right: N_EXPR): N_BINOP("^", left, right, listOf(O_POWER)) {
    override fun asConstant(l: Value, r: Value) = l.toPower(r)
}
class N_MODULUS(left: N_EXPR, right: N_EXPR): N_BINOP("%", left, right, listOf(O_MODULUS)) {
    override fun asConstant(l: Value, r: Value) = l.modulo(r)
}

class N_IN(left: N_EXPR, right: N_EXPR): N_BINOP("in", left, right, listOf(O_IN))

class N_NULLCOAL(left: N_EXPR, right: N_EXPR): N_BINOP("?:", left, right, listOf(O_NULLCOAL))

class N_CMP_EQ(left: N_EXPR, right: N_EXPR): N_BINOP("==", left, right, listOf(O_CMP_EQ))
class N_CMP_NEQ(left: N_EXPR, right: N_EXPR): N_BINOP("!=", left, right, listOf(O_CMP_EQ, O_NEGATE))
class N_CMP_GT(left: N_EXPR, right: N_EXPR): N_BINOP(">", left, right, listOf(O_CMP_GT))
class N_CMP_LT(left: N_EXPR, right: N_EXPR): N_BINOP("<", left, right, listOf(O_CMP_LT))
class N_CMP_GE(left: N_EXPR, right: N_EXPR): N_BINOP(">=", left, right, listOf(O_CMP_GE))
class N_CMP_LE(left: N_EXPR, right: N_EXPR): N_BINOP("<=", left, right, listOf(O_CMP_LE))

class N_AND(left: N_EXPR, right: N_EXPR): N_BINOP("&&", left, right, listOf()) {
    override fun code(c: Coder) = with (c.use(this)) {
        code(left)
        opcode(O_IF)
        jumpForward("andskip")
        code(right)
        opcode(O_JUMP)
        jumpForward("andend")
        setForwardJump("andskip")
        opcode(O_VAL)
        value(false)
        setForwardJump("andend")
    }
}

class N_OR(left: N_EXPR, right: N_EXPR): N_BINOP("||", left, right, listOf()) {
    override fun code(c: Coder) = with (c.use(this)) {
        code(left)
        opcode(O_NEGATE)
        opcode(O_IF)
        jumpForward("orskip")
        code(right)
        opcode(O_JUMP)
        jumpForward("orend")
        setForwardJump("orskip")
        opcode(O_VAL)
        value(true)
        setForwardJump("orend")
    }
}
