package com.dlfsystems.vm

import com.dlfsystems.value.Value
import java.util.*
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.value.*
import com.dlfsystems.vm.VMException.Type.*

// A stack machine for executing a func.

class VM(val code: List<VMWord> = listOf()) {

    // Program Counter: index of the opcode we're about to execute (or argument we're about to fetch).
    private var pc: Int = 0
    // The local stack.
    private val stack = Stack<Value>()
    // Local variables by ID.
    private val variables: MutableMap<Int, Value> = mutableMapOf()

    private fun fail(c: VMException.Type, m: String) { throw VMException(c, "$m [pc: ${pc-1} ${code[pc-1]}]", code[pc].lineNum, code[pc].charNum) }
    private inline fun push(v: Value) = stack.push(v)
    private inline fun pop() = stack.pop()
    private inline fun popTwo() = listOf(stack.pop(), stack.pop())
    private inline fun popThree() = listOf(stack.pop(), stack.pop(), stack.pop())
    private inline fun next() = code[pc++]

    // Given a Context, execute each word of the input code starting from pc=0.
    // Mutate the stack and variables as we go.
    // Return back a Value (VVoid if no explicit return).
    fun execute(context: Context? = null): Value? {
        // Intercept success or failure, so we get to clean up either way.
        var returnValue: Value? = null
        var exception: Exception? = null
        try {
            returnValue = executeCode(context ?: Context())
        } catch (e: Exception) {
            exception = e
        }
        // Win or lose, we clean up after.
        stack.clear()
        variables.clear()
        // Then we succeed or fail.
        exception?.also { throw it }
        return returnValue
    }

    private fun executeCode(c: Context): Value? {
        pc = 0
        val stackLimit = (c.world.getSysValue(c, "stackLimit") as VInt).v
        var ticksLeft = c.ticksLeft
        while (pc < code.size) {

            if (--ticksLeft < 0) fail(E_LIMIT, "tick limit exceeded")
            if (stack.size > stackLimit) fail(E_LIMIT, "stack depth exceeded")

            val word = next()
            when (word.opcode) {

                // Stack ops

                O_DISCARD -> {
                    pop()
                }

                // Value ops

                O_VAL -> {
                    push(next().value!!)
                }
                O_LISTVAL -> {
                    val count = next().intFromV
                    val elements = mutableListOf<Value>()
                    repeat(count) { elements.add(0, pop()) }
                    push(VList(elements))
                }
                O_MAPVAL -> {
                    val count = next().intFromV
                    val entries = mutableMapOf<Value, Value>()
                    repeat(count) { entries.put(pop(), pop()) }
                    push(VMap.make(entries))
                }
                O_INDEX -> {
                    val (a2, a1) = popTwo()
                    a1.getIndex(c, a2)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot index into ${a1.type} with ${a2.type}")
                }
                O_RANGE -> {
                    val (a3, a2, a1) = popThree()
                    a1.getRange(c, a2, a3)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot range into ${a1.type} with ${a2.type}..${a3.type}")
                }

                // Control flow ops

                O_IF -> {
                    val elseAddr = next().address!!
                    val condition = pop()
                    if (condition.isFalse()) pc = elseAddr
                }
                O_JUMP -> {
                    val addr = next().address!!
                    pc = addr
                }
                O_RETURN -> {
                    if (stack.isEmpty()) fail(E_SYS, "no return value on stack!")
                    if (stack.size > 1) fail(E_SYS, "stack polluted on return!")
                    return pop()
                }
                O_RETURNNULL -> {
                    if (stack.isNotEmpty()) fail(E_SYS, "stack polluted on return!")
                    return null
                }
                O_FAIL -> {
                    val a = pop()
                    fail(E_USER, a.asString())
                }

                // Func ops

                O_CALL -> {
                    c.ticksLeft = ticksLeft
                    // get func location, name, args
                    // put our frame on the callstack
                    // call the func for return val
                    // pop our frame off the callstack
                    // push return val
                    ticksLeft = c.ticksLeft
                }

                // Variable ops

                O_GETVAR -> {
                    val varID = next().intFromV
                    variables[varID]?.also { push(it) }
                        ?: fail(E_VARNF, "variable not found")
                }
                O_SETVAR -> {
                    val varID = next().intFromV
                    val a1 = pop()
                    variables[varID] = a1
                }
                O_SETVARI -> {
                    val varID = next().intFromV
                    val (a2, a1) = popTwo()
                    if (!variables[varID]!!.setIndex(c, a2, a1))
                        fail(E_TYPE, "cannot index into ${variables[varID]!!.type} with ${a2.type}")
                }
                O_INCVAR, O_DECVAR -> {
                    val varID = next().intFromV
                    variables[varID]?.also {
                        if (it is VInt)
                            variables[varID] = VInt(it.v + if (word.opcode == O_INCVAR) 1 else -1)
                        else fail(E_TYPE, "cannot increment ${it.type}")
                    } ?: fail(E_VARNF, "variable not found")
                }

                // Iterator ops

                O_ITERSIZE -> {
                    val a1 = pop()
                    a1.iterableSize()?.also {
                        push(VInt(it))
                    } ?: fail(E_TYPE, "cannot iterate ${a1.type}")
                }
                O_ITERPICK -> {
                    val sourceID = next().intFromV
                    val indexID = next().intFromV
                    variables[sourceID]!!.getIndex(c, variables[indexID] as VInt)?.also { push(it) }
                        ?: fail(E_TYPE, "cannot iterate")
                }

                // Property ops

                O_GETPROP -> {
                    val (a2, a1) = popTwo()
                    if (a2 is VString) {
                        a1.getProp(c, a2.v)?.also { push(it) }
                            ?: fail(E_PROPNF, "property not found")
                    } else fail(E_PROPNF, "property name must be string")
                }
                O_SETPROP -> {
                    val (a3, a2, a1) = popThree()
                    if (!a2.setProp(c, (a3 as VString).v, a1))
                        fail(E_PROPNF, "property not found")
                }
                O_GETTRAIT -> {
                    val a1 = pop()
                    if (a1 is VString) {
                        c?.getTrait(a1.v)?.also { push(VTrait(it.id)) }
                            ?: fail (E_TRAITNF, "trait not found")
                    } else fail(E_TRAITNF, "trait name must be string")
                }

                // Boolean ops

                O_NEGATE -> {
                    val a1 = pop()
                    a1.negate()?.also { push(it) }
                        ?: fail(E_TYPE, "cannot negate ${a1.type}")
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
                        ?: fail(E_TYPE, "cannot add ${a1.type} and ${a2.type}")
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
        return null
    }

}

// An atom of VM opcode memory.
// Can hold an Opcode, a Value, or an int representing a memory address (for jumps).
// TODO: rework this as a sealed class like Value
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
