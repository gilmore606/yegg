package com.dlfsystems.yegg.compiler.ast.expr.identifier

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.vm.Opcode.O_CALL
import com.dlfsystems.yegg.vm.Opcode.O_IFNULL

class N_VERBREF(
    val isNullsafe: Boolean,
    val left: N_EXPR,
    val right: N_EXPR,
    val args: List<N_EXPR>
): N_EXPR() {

    override fun toString() = "$left.$right($args)"
    override fun kids() = mutableListOf(left, right).apply { addAll(args) }

    override fun identify() { (right as? N_IDENTIFIER)?.markAsVerb() }

    override fun code(c: Coder) = with (c.use(this)) {
        code(left)
        if (isNullsafe) {
            opcode(O_IFNULL)
            jumpForward("skipref")
        }
        args.forEach { code(it) }
        code(right)
        opcode(O_CALL)
        value(args.size)
        setForwardJump("skipref")
    }

}
