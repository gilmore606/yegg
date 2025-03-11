package com.dlfsystems.vm

import java.util.*
import com.dlfsystems.vm.Opcode.*
import com.dlfsystems.vm.Value.Type.*

// A virtual machine for executing a func.

class VM(val code: List<VMCell>) {

    private val stack = Stack<Value>()
    private var pc: Int = 0

    private fun fail(m: String) { throw VMException(m, code[pc].lineNum, code[pc].charNum) }


    fun execute(): Value {
        pc = 0
        while (pc < code.size) {
            val opcode = code[pc]
            if (!opcode.isOpcode()) fail("!!execution reached non-opcode!!")
            when (opcode.opcode) {
                O_PUSH -> {
                    pc++
                    code[pc].value?.also { stack.push(it) } ?: fail("!!PUSH had no value!!")
                }
                O_NEGATE -> {
                    val value = stack.pop()
                    when (value.type) {
                        INT -> stack.push(Value(INT, intValue = 0 - value.intValue!!))
                        FLOAT -> stack.push(Value(FLOAT, floatValue = 0f - value.floatValue!!))
                        else -> fail("cannot negate non-numeric value")
                    }
                }
                O_ADD -> {
                    val value1 = stack.pop()
                    val value2 = stack.pop()
                    if (value1.type != value2.type) fail("cannot add different types")
                    when (value1.type) {
                        INT -> stack.push(Value(INT, intValue = value1.intValue!! + value2.intValue!!))
                        FLOAT -> stack.push(Value(FLOAT, floatValue = value1.floatValue!! - value2.floatValue!!))
                        STRING -> stack.push(Value(STRING, stringValue = value1.stringValue!! + value2.stringValue!!))
                        else -> fail("cannot add type ${value1.type}")
                    }
                }
                O_MULT -> {
                    val value1 = stack.pop()
                    val value2 = stack.pop()
                    if (value1.type != value2.type) fail("cannot multiply different types")
                    when (value1.type) {
                        INT -> stack.push(Value(INT, intValue = value1.intValue!! * value2.intValue!!))
                        FLOAT -> stack.push(Value(FLOAT, floatValue = value1.floatValue!! * value2.floatValue!!))
                        else -> fail("cannot multiply type ${value1.type}")
                    }
                }
                O_DIV -> {
                    val value1 = stack.pop()
                    val value2 = stack.pop()
                    if (value1.type != value2.type) fail("cannot divide different types")
                    when (value1.type) {
                        INT -> if (value2.intValue == 0) fail("divide by zero") else stack.push(Value(INT, intValue = value1.intValue!! / value2.intValue!!))
                        FLOAT -> if (value2.floatValue == 0f) fail("divide by zero") else stack.push(Value(FLOAT, floatValue = value1.floatValue!! / value2.floatValue!!))
                        else -> fail("cannot divide type ${value1.type}")
                    }
                }
                O_RETURN -> {
                    if (stack.isEmpty()) return Value(VOID)
                    return stack.pop()
                }
                else -> fail("!!unknown opcode: $opcode")
            }
            pc++
        }
        return Value(VOID)
    }

}

class VMCell(
    val lineNum: Int, val charNum: Int,
    val opcode: Opcode? = null, val value: Value? = null
) {
    fun isOpcode() = opcode != null
    fun isValue() = value != null

    override fun toString() = opcode?.toString() ?: value?.toString() ?: "!!NULL!!"
}
