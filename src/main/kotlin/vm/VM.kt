package com.dlfsystems.vm

import java.util.*
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.Value.Type.*
import com.dlfsystems.vm.VMException.Type.*

// A stack machine for executing a func.

class VM(val code: List<VMWord>) {

    // Program Counter: index of the opcode we're about to execute (or argument we're about to fetch).
    private var pc: Int = 0
    // The local stack.
    private val stack = Stack<Value>()
    // Local variables by ID.
    private val variables: MutableMap<Int, Value> = mutableMapOf()

    private fun fail(c: VMException.Type, m: String) { throw VMException(c, m, code[pc].lineNum, code[pc].charNum) }
    private inline fun push(v: Value) = stack.push(v)
    private inline fun pop() = stack.pop()
    private inline fun popTwo() = listOf(stack.pop(), stack.pop())
    private inline fun next() = code[pc++]

    fun execute(context: Context? = null): Value {
        pc = 0
        stack.clear()
        variables.clear()
        while (pc < code.size) {
            val opcode = next()
            when (opcode.opcode) {
                O_LITERAL -> {
                    push(next().value!!)
                }
                O_DISCARD -> {
                    pop()
                }
                O_STORE -> {
                    val varID = next().intValue
                    val a1 = pop()
                    variables[varID] = a1
                }
                O_FETCH -> {
                    val varID = next().intValue
                    variables[varID]?.also { push(it) } ?: fail(E_VARNF, "variable not found")
                }
                O_NEGATE -> {
                    val a1 = pop()
                    when (a1.type) {
                        INT -> push(intValue(0 - a1.intV!!))
                        FLOAT -> push(floatValue(0f - a1.floatV!!))
                        BOOL -> push(boolValue(!a1.boolV!!))
                        else -> fail(E_TYPE, "cannot negate ${a1.type}")
                    }
                }
                O_AND -> {
                    val (a2, a1) = popTwo()
                    if (a1.type != BOOL || a2.type != BOOL) fail(E_TYPE, "cannot AND ${a1.type} and ${a2.type}")
                    push(boolValue(a1.boolV!! && a2.boolV!!))
                }
                O_OR -> {
                    val (a2, a1) = popTwo()
                    if (a1.type != BOOL || a2.type != BOOL) fail(E_TYPE, "cannot OR ${a1.type} and ${a2.type}")
                    push(boolValue(a1.boolV!! || a2.boolV!!))
                }
                O_CMP_EQ -> {
                    // TODO: limit and convert types
                    val (a2, a1) = popTwo()
                    push(boolValue(a1.equals(a2)))
                }
                O_CMP_GT -> {
                    // TODO: limit and convert types
                    val (a2, a1) = popTwo()
                    push(boolValue(a1.greaterThan(a2)))
                }
                O_CMP_GE -> {
                    // TODO: limit and convert types
                    val (a2, a1) = popTwo()
                    push(boolValue(a1.greaterOrEqual(a2)))
                }
                O_ADD -> {
                    val (a2, a1) = popTwo()
                    when (a1.type) {
                        INT -> when (a2.type) {
                            INT -> push(intValue(a1.intV!! + a2.intV!!))
                            FLOAT -> push(floatValue(a1.intV!!.toFloat() + a2.floatV!!))
                            STRING -> push(stringValue(a1.intV!!.toString() + a2.stringV!!))
                            else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                        }
                        FLOAT -> when (a2.type) {
                            INT -> push(floatValue(a1.intV!!.toFloat() + a2.floatV!!))
                            FLOAT -> push(floatValue(a1.floatV!! - a2.floatV!!))
                            STRING -> push(stringValue(a1.floatV!!.toString() + a2.stringV!!))
                            else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                        }
                        STRING -> when (a2.type) {
                            INT -> push(stringValue(a1.stringV!! + a2.intV!!.toString()))
                            FLOAT -> push(stringValue(a1.stringV!! + a2.floatV!!.toString()))
                            STRING -> push(stringValue(a1.stringV!! + a2.stringV!!))
                            else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                        }
                        else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                    }
                }
                O_MULT -> {
                    val (a2, a1) = popTwo()
                    when (a1.type) {
                        INT -> when (a2.type) {
                            INT -> push(intValue(a1.intV!! * a2.intV!!))
                            FLOAT -> push(floatValue(a1.intV!! * a2.floatV!!))
                            else -> fail(E_TYPE, "cannot multiply ${a1.type} and ${a2.type}")
                        }
                        FLOAT -> when (a2.type) {
                            INT -> push(floatValue(a1.floatV!! * a2.intV!!.toFloat()))
                            FLOAT -> push(floatValue(a1.floatV!! * a2.floatV!!))
                            else -> fail(E_TYPE, "cannot multiply ${a1.type} and ${a2.type}")
                        }
                        else -> fail(E_TYPE, "cannot multiply ${a1.type} and ${a2.type}")
                    }
                }
                O_DIV -> {
                    val (a2, a1) = popTwo()
                    if (a2.intV == 0 || a2.floatV == 0f) fail(E_DIV, "divide by zero")
                    when (a1.type) {
                        INT -> when (a2.type) {
                            INT -> push(intValue(a1.intV!! / a2.intV!!))
                            FLOAT -> push(floatValue(a1.intV!! / a2.floatV!!))
                            else -> fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                        }
                        FLOAT -> when (a2.type) {
                            INT -> push(floatValue(a1.floatV!! / a2.intV!!))
                            FLOAT -> push(floatValue(a1.floatV!! / a2.floatV!!))
                            else -> fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                        }
                        else -> fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                    }
                }
                O_IF -> {
                    val elseAddr = next().address!!
                    val condition = pop()
                    if (condition.isFalse()) {
                        pc = elseAddr
                    }
                }
                O_JUMP -> {
                    val addr = next().address!!
                    pc = addr
                }
                O_RETURN -> {
                    return if (stack.isEmpty()) voidValue() else pop()
                }
                else -> fail(E_SYS, "unknown opcode $opcode")
            }
        }
        return voidValue()
    }

}

class VMWord(
    val lineNum: Int, val charNum: Int,
    val opcode: Opcode? = null, val value: Value? = null, var address: Int? = null
) {
    fun fillAddress(newAddress: Int) { address = newAddress }
    override fun toString() = opcode?.toString() ?: value?.toString() ?: address?.let { "<$it>" } ?: "!!NULL!!"
    val intValue: Int
        get() = value!!.intV!!
}

class VMCallstack {
    class Call()

    private val stack = Stack<Call>()
}
