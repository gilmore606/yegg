package com.dlfsystems.vm

import java.util.*
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.Value.Type.*

// A stack machine for executing a func.

class VM(val code: List<VMCell>) {

    private val stack = Stack<Value>()
    private var pc: Int = 0

    private fun fail(m: String) { throw VMException(m, code[pc].lineNum, code[pc].charNum) }

    fun execute(): Value {
        pc = 0
        stack.clear()
        while (pc < code.size) {
            val opcode = code[pc++]
            when (opcode.opcode) {
                O_PUSH -> {
                    code[pc++].value?.also { stack.push(it) } ?: fail("!!PUSH had no value!!")
                }
                O_DISCARD -> {
                    stack.pop()
                }
                O_NEGATE -> {
                    val value = stack.pop()
                    when (value.type) {
                        INT -> stack.push(Value(INT, intValue = 0 - value.intValue!!))
                        FLOAT -> stack.push(Value(FLOAT, floatValue = 0f - value.floatValue!!))
                        BOOL -> stack.push(Value(BOOL, boolValue = !value.boolValue!!))
                        else -> fail("cannot negate ${value.type}")
                    }
                }
                O_CMP_EQ -> {
                    val a2 = stack.pop()
                    val a1 = stack.pop()
                    stack.push(Value(BOOL, boolValue = (a1.equals(a2))))
                }
                O_CMP_GT -> {
                    val a2 = stack.pop()
                    val a1 = stack.pop()
                    stack.push(Value(BOOL, boolValue = (a1.greaterThan(a2))))
                }
                O_CMP_GE -> {
                    val a2 = stack.pop()
                    val a1 = stack.pop()
                    stack.push(Value(BOOL, boolValue = (a1.greaterOrEqual(a2))))
                }
                O_ADD -> {
                    val a2 = stack.pop()
                    val a1 = stack.pop()
                    if (a1.type != a2.type) fail("cannot add different types")
                    when (a1.type) {
                        INT -> stack.push(Value(INT, intValue = a1.intValue!! + a2.intValue!!))
                        FLOAT -> stack.push(Value(FLOAT, floatValue = a1.floatValue!! - a2.floatValue!!))
                        STRING -> stack.push(Value(STRING, stringValue = a1.stringValue!! + a2.stringValue!!))
                        else -> fail("cannot add type ${a1.type}")
                    }
                }
                O_MULT -> {
                    val a2 = stack.pop()
                    val a1 = stack.pop()
                    if (a1.type != a2.type) fail("cannot multiply different types")
                    when (a1.type) {
                        INT -> stack.push(Value(INT, intValue = a1.intValue!! * a2.intValue!!))
                        FLOAT -> stack.push(Value(FLOAT, floatValue = a1.floatValue!! * a2.floatValue!!))
                        else -> fail("cannot multiply type ${a1.type}")
                    }
                }
                O_DIV -> {
                    val a2 = stack.pop()
                    val a1 = stack.pop()
                    if (a1.type != a2.type) fail("cannot divide different types")
                    when (a1.type) {
                        INT -> if (a2.intValue == 0) fail("divide by zero") else stack.push(Value(INT, intValue = a1.intValue!! / a2.intValue!!))
                        FLOAT -> if (a2.floatValue == 0f) fail("divide by zero") else stack.push(Value(FLOAT, floatValue = a1.floatValue!! / a2.floatValue!!))
                        else -> fail("cannot divide type ${a1.type}")
                    }
                }
                O_IF -> {
                    val elseAddr = code[pc++].address!!
                    val condition = stack.pop()
                    if (condition.type == BOOL) {
                        if (condition.boolValue == false) {
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
                    return stack.pop()
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
