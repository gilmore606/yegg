package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.compiler.ast.statement.N_STATEMENT
import com.dlfsystems.yegg.vm.Opcode.*

// try { block } catch [(E_TYPE,...)] { e -> block }
class N_TRY(
    val tryBlock: N_STATEMENT,
    val errors: List<N_EXPR>,
    val catchBlock: N_STATEMENT?,
    val errorName: N_IDENTIFIER? = null,
): N_EXPR() {

    override fun kids() = buildList {
        add(tryBlock)
        addAll(errors)
        catchBlock?.also { add(it) }
        errorName?.also { add(it) }
    }

    override fun code(c: Coder) = with (c.use(this)) {
        errors.forEach { code(it) }
        opcode(O_TRY)
        value(errors.size)
        value(errorName?.variableID ?: -1)
        jumpForward("catch")
        code(tryBlock)
        opcode(O_TRYEND)
        opcode(O_JUMP)
        jumpForward("proceed")
        setForwardJump("catch")
        catchBlock?.also { code(it) }
        setForwardJump("proceed")
    }

}
