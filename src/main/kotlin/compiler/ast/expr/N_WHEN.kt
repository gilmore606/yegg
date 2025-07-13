package com.dlfsystems.yegg.compiler.ast.expr

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.Node
import com.dlfsystems.yegg.compiler.ast.statement.N_EXPRSTATEMENT
import com.dlfsystems.yegg.vm.Opcode.*

// when [expr] { option1 -> expr  option 2 -> { .... expr } else -> ...}
class N_WHEN(val subject: N_EXPR?, val options: List<Pair<N_EXPR?, Node>>, val asStatement: Boolean = false): N_EXPR() {
    override fun kids() = buildList {
        subject?.also { add(it) }
        addAll(options.mapNotNull { it.first })
        addAll(options.map { it.second })
    }

    override fun code(c: Coder) = with (c.use(this)) {
        // non-null match options
        options.filter { it.first != null }.forEachIndexed { n, o ->
            code(o.first!!)
            subject?.also {
                code(it)
                opcode(O_CMP_EQ)
            }
            opcode(O_IF)
            jumpForward("skip$n")
            code(o.second)
            if (asStatement && o.second is N_EXPRSTATEMENT) opcode(O_DISCARD)
            opcode(O_JUMP)
            jumpForward("end")
            setForwardJump("skip$n")
        }
        // else option
        options.firstOrNull { it.first == null }?.also { o ->
            code(o.second)
        }
        setForwardJump("end")
    }
}
