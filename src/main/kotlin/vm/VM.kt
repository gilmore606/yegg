package com.dlfsystems.vm

import java.util.*
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.Value.Type.*

// A stack machine for executing a func.

class VM(val code: List<VMCell>) {

    // Program Counter; index of the opcode we're about to execute.
    private var pc: Int = 0
    // The local stack.
    private val stack = Stack<Value>()
    // Local variables.
    private val vars: MutableMap<Int, Value> = mutableMapOf()

    private fun fail(m: String) { throw VMException(m, code[pc].lineNum, code[pc].charNum) }
    private inline fun push(v: Value) = stack.push(v)
    private inline fun popArg() = stack.pop()
    private inline fun popTwoArgs() = listOf(stack.pop(), stack.pop())

    fun execute(): Value {
        pc = 0
        stack.clear()
        vars.clear()
        while (pc < code.size) {
            val opcode = code[pc++]
            when (opcode.opcode) {
                O_LITERAL -> {
                    code[pc++].value?.also { push(it) } ?: fail("!!PUSH had no value!!")
                }
                O_DISCARD -> {
                    stack.pop()
                }
                O_NEGATE -> {
                    val a = popArg()
                    when (a.type) {
                        INT -> push(Value(INT, intV = 0 - a.intV!!))
                        FLOAT -> push(Value(FLOAT, floatV = 0f - a.floatV!!))
                        BOOL -> push(Value(BOOL, boolV = !a.boolV!!))
                        else -> fail("cannot negate ${a.type}")
                    }
                }
                O_CMP_EQ -> {
                    val (a2, a1) = popTwoArgs()
                    push(Value(BOOL, boolV = (a1.equals(a2))))
                }
                O_CMP_GT -> {
                    val (a2, a1) = popTwoArgs()
                    push(Value(BOOL, boolV = (a1.greaterThan(a2))))
                }
                O_CMP_GE -> {
                    val (a2, a1) = popTwoArgs()
                    push(Value(BOOL, boolV = (a1.greaterOrEqual(a2))))
                }
                O_ADD -> {
                    val (a2, a1) = popTwoArgs()
                    if (a1.type != a2.type) fail("cannot add different types")
                    when (a1.type) {
                        INT -> push(Value(INT, intV = a1.intV!! + a2.intV!!))
                        FLOAT -> push(Value(FLOAT, floatV = a1.floatV!! - a2.floatV!!))
                        STRING -> push(Value(STRING, stringV = a1.stringV!! + a2.stringV!!))
                        else -> fail("cannot add type ${a1.type}")
                    }
                }
                O_MULT -> {
                    val (a2, a1) = popTwoArgs()
                    if (a1.type != a2.type) fail("cannot multiply different types")
                    when (a1.type) {
                        INT -> push(Value(INT, intV = a1.intV!! * a2.intV!!))
                        FLOAT -> push(Value(FLOAT, floatV = a1.floatV!! * a2.floatV!!))
                        else -> fail("cannot multiply type ${a1.type}")
                    }
                }
                O_DIV -> {
                    val (a2, a1) = popTwoArgs()
                    if (a1.type != a2.type) fail("cannot divide different types")
                    when (a1.type) {
                        INT -> if (a2.intV == 0) fail("divide by zero") else
                            push(Value(INT, intV = a1.intV!! / a2.intV!!))
                        FLOAT -> if (a2.floatV == 0f) fail("divide by zero") else
                            push(Value(FLOAT, floatV = a1.floatV!! / a2.floatV!!))
                        else -> fail("cannot divide type ${a1.type}")
                    }
                }
                O_IF -> {
                    val elseAddr = code[pc++].address!!
                    val condition = popArg()
                    if (condition.type == BOOL) {
                        if (condition.boolV == false) {
                            pc = elseAddr
                        }
                    }
                }
                O_JUMP -> {
                    val addr = code[pc++].address!!
                    pc = addr
                }
                O_RETURN -> {
                    if (stack.isEmpty()) return Value(VOID)
                    return popArg()
                }
                else -> fail("!!unknown opcode: $opcode")
            }
        }
        return Value(VOID)
    }

}

class VMCell(
    val lineNum: Int, val charNum: Int,
    val opcode: Opcode? = null, val value: Value? = null, var address: Int? = null
) {
    fun fillAddress(newAddress: Int) { address = newAddress }
    override fun toString() = opcode?.toString() ?: value?.toString() ?: address?.let { "a<$it>" } ?: "!!NULL!!"
}
