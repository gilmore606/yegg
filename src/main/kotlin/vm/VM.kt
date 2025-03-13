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
    // Return back a Value (VVoid if no explicit return).
    fun execute(context: Context? = null): Value {
        var returnValue: Value? = null
        var exception: Exception? = null
        try {
            returnValue = executeCode(context)
        } catch (e: Exception) {
            exception = e
        }
        // Win or lose, we clean up after.
        stack.clear()
        variables.clear()
        // Then we do what we were going to do.
        exception?.also { throw it }
        return returnValue!!
    }

    private fun executeCode(context: Context?): Value {
        pc = 0
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
                    val varID = next().intFromV
                    val a1 = pop()
                    variables[varID] = a1
                }
                O_FETCH -> {
                    val varID = next().intFromV
                    variables[varID]?.also { push(it) }
                        ?: fail(E_VARNF, "variable not found")
                }
                O_INCVAR, O_DECVAR -> {
                    val varID = next().intFromV
                    variables[varID]?.also {
                        if (it is VInt)
                            variables[varID] = VInt(it.v + if (word.opcode == O_INCVAR) 1 else -1)
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
                O_CMP_EQ, O_CMP_GT, O_CMP_GE, O_CMP_LT, O_CMP_LE -> {
                    val (a2, a1) = popTwo()
                    when (word.opcode) {
                        O_CMP_EQ -> push(VBool(a1.cmpEq(a2)))
                        O_CMP_GT -> push(VBool(a1.cmpGt(a2)))
                        O_CMP_GE -> push(VBool(a1.cmpGe(a2)))
                        O_CMP_LT -> push(VBool(a1.cmpLt(a2)))
                        O_CMP_LE -> push(VBool(a1.cmpLe(a2)))
                        else -> { }
                    }
                }

                // Math ops

                O_ADD -> {
                    val (a2, a1) = popTwo()
                    a1.plus(a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot add types ${a1.type} and ${a2.type}")
                }
                O_MULT -> {
                    val (a2, a1) = popTwo()
                    a1.multiply(a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot multiply ${a1.type} and ${a2.type}")
                }
                O_DIV -> {
                    val (a2, a1) = popTwo()
                    if (a2.isZero() || a1.isZero()) fail(E_DIV, "divide by zero")
                    a1.divide(a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot divide ${a1.type} and ${a2.type}")
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
    // In compilation, an address word may be written before the address it points to is known.
    // fillAddress is called to set it once calculated.
    fun fillAddress(newAddress: Int) { address = newAddress }

    override fun toString() = opcode?.toString() ?: value?.toString() ?: address?.let { "<$it>" } ?: "!!NULL!!"

    // If this is known to be an int opcode arg, just get the int value.
    val intFromV: Int
        get() = (value as VInt).v
}

// A stack recording the history of a chain of nested func calls, held by a Context.
class VMCallstack {
    class Call()

    private val stack = Stack<Call>()
}
