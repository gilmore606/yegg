package com.dlfsystems.vm

// An opcode representing an instruction for a VM.

enum class Opcode(val literal: String, val argCount: Int = 0) {

    O_PUSH("push", 1),
    O_NEGATE("negate"),
    O_ADD("add"),
    O_MULT("mult"),
    O_DIV("div"),
    O_POWER("power"),
    O_MODULUS("modulus"),

    O_RETURN("return"),
}
