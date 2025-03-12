package com.dlfsystems.vm

// An opcode representing an instruction for a VM.

enum class Opcode(val literal: String, val argCount: Int = 0) {

    // stack ops
    O_LITERAL("push", 1),
    O_DISCARD("discard"),

    // flow ops
    O_IF("if", 1),
    O_JUMP("jump", 1),

    // math ops
    O_NEGATE("negate"),
    O_ADD("add"),
    O_MULT("mult"),
    O_DIV("div"),
    O_POWER("power"),
    O_MODULUS("modulus"),

    // boolean ops
    O_CMP_EQ("cmpeq"),  // ==
    O_CMP_GT("cmpgt"),  // >
    O_CMP_GE("cmpge"),  // >=

    // func ops
    O_RETURN("return"),
}
