package com.dlfsystems.yegg.compiler.ast.expr.literal

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.ast.expr.N_EXPR
import com.dlfsystems.yegg.value.*
import com.dlfsystems.yegg.vm.Opcode.*
import com.dlfsystems.yegg.vm.VMException
import com.dlfsystems.yegg.world.Obj

// A literal value appearing in code.

abstract class N_LITERAL: N_EXPR() {
    open fun codeValue(coder: Coder) { }

    override fun code(c: Coder) {
        with (c.use(this)) {
            opcode(O_VAL)
            codeValue(c)
        }
    }
}

class N_LITERAL_BOOLEAN(val value: Boolean): N_LITERAL() {
    override fun toString() = if (value) "true" else "false"
    override fun codeValue(coder: Coder) { coder.value(value) }
    override fun constantValue() = VBool(value)
}

class N_LITERAL_INTEGER(val value: Int): N_LITERAL() {
    override fun toString() = "$value"
    override fun codeValue(coder: Coder) { coder.value(value) }
    override fun constantValue() = VInt(value)
}

class N_LITERAL_FLOAT(val value: Float): N_LITERAL() {
    override fun toString() = "$value"
    override fun codeValue(coder: Coder) { coder.value(value) }
    override fun constantValue() = VFloat(value)
}

class N_LITERAL_OBJ(val objID: Obj.ID): N_LITERAL() {
    override fun toString() = "#$id"
    override fun codeValue(coder: Coder) { coder.value(objID) }
    override fun constantValue() = VObj(objID)
}

class N_LITERAL_STRING(val value: String): N_LITERAL() {
    override fun toString() = "\"$value\""
    override fun codeValue(coder: Coder) { coder.value(value) }
    override fun constantValue() = VString(value)
}

class N_LITERAL_ERR(val value: VMException.Type): N_LITERAL() {
    override fun toString() = "\"$value\""
    override fun codeValue(coder: Coder) { coder.value(value) }
    override fun constantValue() = VErr(value)
}
