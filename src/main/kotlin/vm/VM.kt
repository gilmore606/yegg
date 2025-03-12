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
                    variables[varID]?.also { push(it) } ?: fail("variable not found")
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
                    when (a1.type) {
                        INT -> when (a2.type) {
                            INT -> push(intValue(a1.intV!! + a2.intV!!))
                            FLOAT -> push(floatValue(a1.intV!!.toFloat() + a2.floatV!!))
                            STRING -> push(stringValue(a1.intV!!.toString() + a2.stringV!!))
                            else -> fail("type error")
                        }
                        FLOAT -> when (a2.type) {
                            INT -> push(floatValue(a1.intV!!.toFloat() + a2.floatV!!))
                            FLOAT -> push(floatValue(a1.floatV!! - a2.floatV!!))
                            STRING -> push(stringValue(a1.floatV!!.toString() + a2.stringV!!))
                            else -> fail("type error")
                        }
                        STRING -> when (a2.type) {
                            INT -> push(stringValue(a1.stringV!! + a2.intV!!.toString()))
                            FLOAT -> push(stringValue(a1.stringV!! + a2.floatV!!.toString()))
                            STRING -> push(stringValue(a1.stringV!! + a2.stringV!!))
                            else -> fail("type error")
                        }
                        else -> fail("type error")
                    }
                }
                O_MULT -> {
                    val (a2, a1) = popTwoArgs()
                    when (a1.type) {
                        INT -> when (a2.type) {
                            INT -> push(intValue(a1.intV!! * a2.intV!!))
                            FLOAT -> push(floatValue(a1.intV!! * a2.floatV!!))
                            else -> fail("type error")
                        }
                        FLOAT -> when (a2.type) {
                            INT -> push(floatValue(a1.floatV!! * a2.intV!!.toFloat()))
                            FLOAT -> push(floatValue(a1.floatV!! * a2.floatV!!))
                            else -> fail("type error")
                        }
                        else -> fail("type error")
                    }
                }
                O_DIV -> {
                    val (a2, a1) = popTwoArgs()
                    if (a2.intV == 0 || a2.floatV == 0f) fail("divide by zero")
                    when (a1.type) {
                        INT -> when (a2.type) {
                            INT -> push(intValue(a1.intV!! / a2.intV!!))
                            FLOAT -> push(floatValue(a1.intV!! / a2.floatV!!))
                            else -> fail("type error")
                        }
                        FLOAT -> when (a2.type) {
                            INT -> push(floatValue(a1.floatV!! / a2.intV!!))
                            FLOAT -> push(floatValue(a1.floatV!! / a2.floatV!!))
                            else -> fail("type error")
                        }
                        else -> fail("type error")
                    }
                }
                O_IF -> {
                    val elseAddr = code[pc++].address!!
                    val condition = popArg()
                    if (condition.type == BOOL) {
                        if (condition.boolV == false) {
                            pc = elseAddr
                        }
                    } else fail("non-boolean if condition")
                }
                O_JUMP -> {
                    val addr = code[pc++].address!!
                    pc = addr
                }
                O_RETURN -> {
                    return if (stack.isEmpty()) voidValue() else popArg()
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
