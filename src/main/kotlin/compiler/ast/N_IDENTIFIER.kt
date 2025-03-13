package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.vm.Opcode.*


class N_IDENTIFIER(val name: String): N_VALUE() {
    enum class Type { VARIABLE, PROPREF, FUNCREF }
    var type: Type = Type.VARIABLE
    fun isVariable() = type == Type.VARIABLE
    var variableID: Int? = null

    override fun toText() = "$name"

    override fun code(coder: Coder) {
        coder.code(this, O_FETCHVAR)
        coder.value(this, variableID!!)
    }

    override fun codeAssign(coder: Coder) {
        coder.code(this, O_STOREVAR)
        coder.value(this, variableID!!)
    }
}
