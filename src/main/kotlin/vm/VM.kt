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

    // Given a Context, execute each word of the input code starting from pc=0.
    // Mutate the stack and variables as we go.
    // Return back a Value (type VOID if no explicit return).

    fun execute(context: Context? = null): Value {
        pc = 0
        stack.clear()
        variables.clear()
        while (pc < code.size) {
            val word = next()
            when (word.opcode) {

                // Stack ops

                O_DISCARD -> {
                    pop()
                }
                O_LITERAL -> {
                    push(next().value!!)
                }

                // Control flow ops

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
                    return if (stack.isEmpty()) voidV() else pop()
                }

                // Variable ops

                O_STORE -> {
                    val varID = next().asIntValue
                    val a1 = pop()
                    variables[varID] = a1
                }
                O_FETCH -> {
                    val varID = next().asIntValue
                    variables[varID]?.also { push(it) } ?: fail(E_VARNF, "variable not found")
                }
                O_INCVAR, O_DECVAR -> {
                    val varID = next().asIntValue
                    variables[varID]?.also {
                        if (it.type != INT) fail(E_TYPE, "cannot increment ${it.type}")
                        variables[varID] = intV(it.intV!! + if (word.opcode == O_INCVAR) 1 else -1)
                    } ?: fail(E_VARNF, "variable not found")
                }

                // Boolean ops

                O_NEGATE -> {
                    val a1 = pop()
                    when (a1.type) {
                        INT -> push(intV(0 - a1.intV!!))
                        FLOAT -> push(floatV(0f - a1.floatV!!))
                        BOOL -> push(boolV(!a1.boolV!!))
                        else -> fail(E_TYPE, "cannot negate ${a1.type}")
                    }
                }
                O_AND -> {
                    val (a2, a1) = popTwo()
                    if (a1.type != BOOL || a2.type != BOOL) fail(E_TYPE, "cannot AND ${a1.type} and ${a2.type}")
                    push(boolV(a1.boolV!! && a2.boolV!!))
                }
                O_OR -> {
                    val (a2, a1) = popTwo()
                    if (a1.type != BOOL || a2.type != BOOL) fail(E_TYPE, "cannot OR ${a1.type} and ${a2.type}")
                    push(boolV(a1.boolV!! || a2.boolV!!))
                }
                O_CMP_EQ -> {
                    val (a2, a1) = popTwo()
                    push(boolV(if (a1.type == a2.type) a1.equals(a2) else false))
                }
                O_CMP_GT, O_CMP_GE, O_CMP_LT, O_CMP_LE -> {
                    val (a2, a1) = popTwo()
                    if (a1.type != a2.type) fail(E_TYPE, "cannot compare disparate types")
                    when (a1.type) {
                        INT -> when (word.opcode) {
                            O_CMP_GT -> push(boolV(a1.intV!! > a2.intV!!))
                            O_CMP_GE -> push(boolV(a1.intV!! >= a2.intV!!))
                            O_CMP_LT -> push(boolV(a1.intV!! < a2.intV!!))
                            O_CMP_LE -> push(boolV(a1.intV!! <= a2.intV!!))
                            else -> { }
                        }
                        FLOAT -> when (word.opcode) {
                            O_CMP_GT -> push(boolV(a1.floatV!! > a2.floatV!!))
                            O_CMP_GE -> push(boolV(a1.floatV!! >= a2.floatV!!))
                            O_CMP_LT -> push(boolV(a1.floatV!! < a2.floatV!!))
                            O_CMP_LE -> push(boolV(a1.floatV!! <= a2.floatV!!))
                            else -> { }
                        }
                        STRING -> when (word.opcode) {
                            O_CMP_GT -> push(boolV(a1.stringV!! > a2.stringV!!))
                            O_CMP_GE -> push(boolV(a1.stringV!! >= a2.stringV!!))
                            O_CMP_LT -> push(boolV(a1.stringV!! < a2.stringV!!))
                            O_CMP_LE -> push(boolV(a1.stringV!! <= a2.stringV!!))
                            else -> { }
                        }
                        else -> fail(E_TYPE, "cannot compare ${a1.type}")
                    }
                }

                // Math ops

                O_ADD -> {
                    val (a2, a1) = popTwo()
                    when (a1.type) {
                        INT -> when (a2.type) {
                            INT -> push(intV(a1.intV!! + a2.intV!!))
                            FLOAT -> push(floatV(a1.intV!!.toFloat() + a2.floatV!!))
                            STRING -> push(stringV(a1.intV!!.toString() + a2.stringV!!))
                            else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                        }
                        FLOAT -> when (a2.type) {
                            INT -> push(floatV(a1.intV!!.toFloat() + a2.floatV!!))
                            FLOAT -> push(floatV(a1.floatV!! - a2.floatV!!))
                            STRING -> push(stringV(a1.floatV!!.toString() + a2.stringV!!))
                            else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                        }
                        STRING -> when (a2.type) {
                            INT -> push(stringV(a1.stringV!! + a2.intV!!.toString()))
                            FLOAT -> push(stringV(a1.stringV!! + a2.floatV!!.toString()))
                            STRING -> push(stringV(a1.stringV!! + a2.stringV!!))
                            else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                        }
                        else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                    }
                }
                O_MULT -> {
                    val (a2, a1) = popTwo()
                    when (a1.type) {
                        INT -> when (a2.type) {
                            INT -> push(intV(a1.intV!! * a2.intV!!))
                            FLOAT -> push(floatV(a1.intV!! * a2.floatV!!))
                            else -> fail(E_TYPE, "cannot multiply ${a1.type} and ${a2.type}")
                        }
                        FLOAT -> when (a2.type) {
                            INT -> push(floatV(a1.floatV!! * a2.intV!!.toFloat()))
                            FLOAT -> push(floatV(a1.floatV!! * a2.floatV!!))
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
                            INT -> push(intV(a1.intV!! / a2.intV!!))
                            FLOAT -> push(floatV(a1.intV!! / a2.floatV!!))
                            else -> fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                        }
                        FLOAT -> when (a2.type) {
                            INT -> push(floatV(a1.floatV!! / a2.intV!!))
                            FLOAT -> push(floatV(a1.floatV!! / a2.floatV!!))
                            else -> fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                        }
                        else -> fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                    }
                }

                else -> fail(E_SYS, "unknown opcode $word")
            }
        }
        return voidV()
    }

}

// An atom of VM opcode memory.
// Can hold an Opcode, a Value, or an int representing a memory address (for jumps).
class VMWord(
    val lineNum: Int, val charNum: Int,
    val opcode: Opcode? = null, val value: Value? = null, var address: Int? = null
) {
    // In compilation, address may be filled in later by Shaker.
    fun fillAddress(newAddress: Int) { address = newAddress }

    override fun toString() = opcode?.toString() ?: value?.toString() ?: address?.let { "<$it>" } ?: "!!NULL!!"

    val asIntValue: Int
        get() = value!!.intV!!
}

// A stack recording the history of a chain of nested func calls, held by a Context.
class VMCallstack {
    class Call()

    private val stack = Stack<Call>()
}
