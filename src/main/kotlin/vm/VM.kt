package com.dlfsystems.vm

import java.util.*
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.Value.*
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
                    return if (stack.isEmpty()) VVoid() else pop()
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
                        if (it is VInt) variables[varID] = VInt(it.v + if (word.opcode == O_INCVAR) 1 else -1)
                        else fail(E_TYPE, "cannot increment ${it.type}")
                    } ?: fail(E_VARNF, "variable not found")
                }

                // Boolean ops

                O_NEGATE -> {
                    val a1 = pop()
                    when (a1) {
                        is VInt -> push(VInt(0 - a1.v))
                        is VFloat -> push(VFloat(0f - a1.v))
                        is VBool -> push(VBool(!a1.v))
                        else -> fail(E_TYPE, "cannot negate ${a1.type}")
                    }
                }
                O_AND -> {
                    val (a2, a1) = popTwo()
                    if (a1 is VBool && a2 is VBool) push(VBool(a1.v && a2.v))
                    else fail(E_TYPE, "cannot AND ${a1.type} and ${a2.type}")
                }
                O_OR -> {
                    val (a2, a1) = popTwo()
                    if (a1 is VBool && a2 is VBool) push(VBool(a1.v || a2.v))
                    else fail(E_TYPE, "cannot OR ${a1.type} and ${a2.type}")
                }
                O_CMP_EQ -> {
                    val (a2, a1) = popTwo()
                    push(VBool(if (a1.type == a2.type) a1.equals(a2) else false))
                }
                O_CMP_GT, O_CMP_GE, O_CMP_LT, O_CMP_LE -> {
                    val (a2, a1) = popTwo()
                    if (a1.type != a2.type) fail(E_TYPE, "cannot compare disparate types")
                    when (a1) {
                        is VInt -> when (word.opcode) {
                            O_CMP_GT -> push(VBool(a1.v > (a2 as VInt).v))
                            O_CMP_GE -> push(VBool(a1.v >= (a2 as VInt).v))
                            O_CMP_LT -> push(VBool(a1.v < (a2 as VInt).v))
                            O_CMP_LE -> push(VBool(a1.v <= (a2 as VInt).v))
                            else -> { }
                        }
                        is VFloat -> when (word.opcode) {
                            O_CMP_GT -> push(VBool(a1.v > (a2 as VFloat).v))
                            O_CMP_GE -> push(VBool(a1.v >= (a2 as VFloat).v))
                            O_CMP_LT -> push(VBool(a1.v < (a2 as VFloat).v))
                            O_CMP_LE -> push(VBool(a1.v <= (a2 as VFloat).v))
                            else -> { }
                        }
                        is VString -> when (word.opcode) {
                            O_CMP_GT -> push(VBool(a1.v > (a2 as VString).v))
                            O_CMP_GE -> push(VBool(a1.v >= (a2 as VString).v))
                            O_CMP_LT -> push(VBool(a1.v < (a2 as VString).v))
                            O_CMP_LE -> push(VBool(a1.v <= (a2 as VString).v))
                            else -> { }
                        }
                        else -> fail(E_TYPE, "cannot compare ${a1.type}")
                    }
                }

                // Math ops

                O_ADD -> {
                    val (a2, a1) = popTwo()
                    when (a1) {
                        is VInt -> when (a2) {
                            is VInt -> push(VInt(a1.v + a2.v))
                            is VFloat -> push(VFloat(a1.v.toFloat() + a2.v))
                            is VString -> push(VString(a1.v.toString() + a2.v))
                            else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                        }
                        is VFloat -> when (a2) {
                            is VInt -> push(VFloat(a1.v + a2.v.toFloat()))
                            is VFloat -> push(VFloat(a1.v + a2.v))
                            is VString -> push(VString(a1.v.toString() + a2.v))
                            else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                        }
                        is VString -> when (a2) {
                            is VInt -> push(VString(a1.v + a2.v.toString()))
                            is VFloat -> push(VString(a1.v + a2.v.toString()))
                            is VString -> push(VString(a1.v + a2.v))
                            else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                        }
                        else -> fail(E_TYPE, "cannot add ${a1.type} to ${a2.type}")
                    }
                }
                O_MULT -> {
                    val (a2, a1) = popTwo()
                    when (a1) {
                        is VInt -> when (a2) {
                            is VInt -> push(VInt(a1.v * a2.v))
                            is VFloat -> push(VFloat(a1.v.toFloat() * a2.v))
                            else -> fail(E_TYPE, "cannot multiply ${a1.type} and ${a2.type}")
                        }
                        is VFloat -> when (a2) {
                            is VInt -> push(VFloat(a1.v * a2.v.toFloat()))
                            is VFloat -> push(VFloat(a1.v * a2.v))
                            else -> fail(E_TYPE, "cannot multiply ${a1.type} and ${a2.type}")
                        }
                        else -> fail(E_TYPE, "cannot multiply ${a1.type} and ${a2.type}")
                    }
                }
                O_DIV -> {
                    val (a2, a1) = popTwo()
                    if ((a2 is VInt && a2.v == 0) || (a2 is VFloat && a2.v == 0f)) fail(E_DIV, "divide by zero")
                    when (a1) {
                        is VInt -> when (a2) {
                            is VInt -> push(VInt(a1.v / a2.v))
                            is VFloat -> push(VFloat(a1.v.toFloat() / a2.v))
                            else -> fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                        }
                        is VFloat -> when (a2) {
                            is VInt -> push(VFloat(a1.v / a2.v.toFloat()))
                            is VFloat -> push(VFloat(a1.v / a2.v))
                            else -> fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                        }
                        else -> fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
                    }
                }

                else -> fail(E_SYS, "unknown opcode $word")
            }
        }
        return VVoid()
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
        get() = (value as VInt).v
}

// A stack recording the history of a chain of nested func calls, held by a Context.
class VMCallstack {
    class Call()

    private val stack = Stack<Call>()
}
