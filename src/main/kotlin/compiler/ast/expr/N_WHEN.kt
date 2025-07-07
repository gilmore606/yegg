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

    override fun code(c: Coder) {
        // non-null match options
        options.filter { it.first != null }.forEachIndexed { n, o ->
            o.first!!.code(c)
            subject?.also {
                it.code(c)
                c.opcode(this, O_CMP_EQ)
            }
            c.opcode(this, O_IF)
            c.jumpForward(this, "skip$n")
            o.second.code(c)
            if (asStatement && o.second is N_EXPRSTATEMENT) c.opcode(this, O_DISCARD)
            c.opcode(this, O_JUMP)
            c.jumpForward(this, "end")
            c.setForwardJump(this, "skip$n")
        }
        // else option
        options.firstOrNull { it.first == null }?.also { o ->
            o.second.code(c)
        }
        c.setForwardJump(this, "end")
    }
}
