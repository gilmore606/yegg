package com.dlfsystems.compiler.ast.expr

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.ast.Node
import com.dlfsystems.compiler.ast.statement.N_EXPRSTATEMENT
import com.dlfsystems.vm.Opcode.*

// when [expr] { option1 -> expr  option 2 -> { .... expr } else -> ...}
class N_WHEN(val subject: N_EXPR?, val options: List<Pair<N_EXPR?, Node>>, val asStatement: Boolean = false): N_EXPR() {
    override fun kids() = buildList {
        subject?.also { add(it) }
        addAll(options.mapNotNull { it.first })
        addAll(options.map { it.second })
    }

    override fun code(coder: Coder) {
        // non-null match options
        options.filter { it.first != null }.forEachIndexed { n, o ->
            o.first!!.code(coder)
            subject?.also {
                it.code(coder)
                coder.code(this, O_CMP_EQ)
            }
            coder.code(this, O_IF)
            coder.jumpForward(this, "skip$n")
            o.second.code(coder)
            if (asStatement && o.second is N_EXPRSTATEMENT) coder.code(this, O_DISCARD)
            coder.code(this, O_JUMP)
            coder.jumpForward(this, "end")
            coder.setForwardJump(this, "skip$n")
        }
        // else option
        options.firstOrNull { it.first == null }?.also { o ->
            o.second.code(coder)
        }
        coder.setForwardJump(this, "end")
    }
}
