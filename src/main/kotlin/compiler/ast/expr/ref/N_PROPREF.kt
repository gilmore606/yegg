package com.dlfsystems.yegg.compiler.ast.expr.ref

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.vm.Opcode.O_DISCARD
import com.dlfsystems.yegg.vm.Opcode.O_GETPROP
import com.dlfsystems.yegg.vm.Opcode.O_IFNULL
import com.dlfsystems.yegg.vm.Opcode.O_JUMP
import com.dlfsystems.yegg.vm.Opcode.O_SETPROP

class N_PROPREF(
    val isNullsafe: Boolean,
    val left: N_EXPR,
    val right: N_EXPR
): N_EXPR() {

    override fun toString() = "($left.$right)"
    override fun kids() = listOf(left, right)

    override fun identify() { (right as? N_IDENTIFIER)?.markAsProp() }

    override fun code(c: Coder) = with (c.use(this)) {
        code(left)
        if (isNullsafe) {
            opcode(O_IFNULL)
            jumpForward("skipref")
        }
        code(right)
        opcode(O_GETPROP)
        setForwardJump("skipref")
    }

    override fun codeAssign(c: Coder) = with (c.use(this)) {
        code(left)
        if (isNullsafe) {
            opcode(O_IFNULL)
            jumpForward("skipref")
        }
        code(right)
        opcode(O_SETPROP)
        opcode(O_JUMP)
        jumpForward("skipNullpop")
        setForwardJump("skipref")
        opcode(O_DISCARD)
        setForwardJump("skipNullpop")
    }

}
