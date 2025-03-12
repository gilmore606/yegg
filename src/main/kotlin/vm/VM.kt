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
    private val variables: MutableMap<Int, Value> = mutableMapOf()

    private fun fail(m: String) { throw VMException(m, code[pc].lineNum, code[pc].charNum) }
    private inline fun push(v: Value) = stack.push(v)
    private inline fun popArg() = stack.pop()
    private inline fun popTwoArgs() = listOf(stack.pop(), stack.pop())

    fun execute(): Value {
        pc = 0
        stack.clear()
        variables.clear()
        while (pc < code.size) {
            val opcode = code[pc++]
            when (opcode.opcode) {
                O_LITERAL -> {
                    code[pc++].value?.also { push(it) } ?: fail("!!PUSH had no value!!")
                }
                O_DISCARD -> {
                    stack.pop()
                }
                O_STORE -> {
                    val a = popArg()
                    val varID = code[pc++].value!!.intV!!
                    variables[varID] = a
                }
                O_FETCH -> {
                    val varID = code[pc++].value!!.intV!!
                    push(variables[varID]!!)
                }
                O_NEGATE -> {
                    val a = popArg()
                    when (a.type) {
                        INT -> push(intValue(0 - a.intV!!))
                        FLOAT -> push(floatValue(0f - a.floatV!!))
                        BOOL -> push(boolValue(!a.boolV!!))
                        else -> fail("cannot negate ${a.type}")
                    }
                }
                O_CMP_EQ -> {
                    val (a2, a1) = popTwoArgs()
                    push(boolValue(a1.equals(a2)))
                }
                O_CMP_GT -> {
                    val (a2, a1) = popTwoArgs()
                    push(boolValue(a1.greaterThan(a2)))
                }
                O_CMP_GE -> {
                    val (a2, a1) = popTwoArgs()
                    push(boolValue(a1.greaterOrEqual(a2)))
                }
                O_ADD -> {
                    val (a2, a1) = popTwoArgs()
                    if (a1.type != a2.type) fail("cannot add different types")
                    when (a1.type) {
                        INT -> push(intValue(a1.intV!! + a2.intV!!))
                        FLOAT -> push(floatValue(a1.floatV!! - a2.floatV!!))
                        STRING -> push(stringValue(a1.stringV!! + a2.stringV!!))
                        else -> fail("cannot add type ${a1.type}")
                    }
                }
                O_MULT -> {
                    val (a2, a1) = popTwoArgs()
                    if (a1.type != a2.type) fail("cannot multiply different types")
                    when (a1.type) {
                        INT -> push(intValue(a1.intV!! * a2.intV!!))
                        FLOAT -> push(floatValue(a1.floatV!! * a2.floatV!!))
                        else -> fail("cannot multiply type ${a1.type}")
                    }
                }
                O_DIV -> {
                    val (a2, a1) = popTwoArgs()
                    if (a1.type != a2.type) fail("cannot divide different types")
                    when (a1.type) {
                        INT -> if (a2.intV == 0) fail("divide by zero") else
                            push(intValue(a1.intV!! / a2.intV!!))
                        FLOAT -> if (a2.floatV == 0f) fail("divide by zero") else
                            push(floatValue(a1.floatV!! / a2.floatV!!))
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
                    if (stack.isEmpty()) return voidValue()
                    return popArg()
                }
                else -> fail("!!unknown opcode: $opcode")
            }
        }
        return voidValue()
    }

}

class VMCell(
    val lineNum: Int, val charNum: Int,
    val opcode: Opcode? = null, val value: Value? = null, var address: Int? = null
) {
    fun fillAddress(newAddress: Int) { address = newAddress }
    override fun toString() = opcode?.toString() ?: value?.toString() ?: address?.let { "a<$it>" } ?: "!!NULL!!"
}
