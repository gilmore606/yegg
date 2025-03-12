package com.dlfsystems.vm

// An opcode representing an instruction for a VM.

enum class Opcode(val literal: String, val argCount: Int = 0) {

    // stack ops
    O_DISCARD("discard"),
    O_LITERAL("push", 1),

    // flow ops
    O_IF("if", 1),
    O_JUMP("jump", 1),
    O_RETURN("return"),

    // variable ops
    O_STORE("store", 1),
    O_FETCH("fetch", 1),
    O_INCVAR("incvar", 1),
    O_DECVAR("decvar", 1),

    // boolean ops
    O_NEGATE("negate"),
    O_AND("and"),       // &&
    O_OR("or"),         // ||
    O_CMP_EQ("cmpeq"),  // ==
    O_CMP_GT("cmpgt"),  // >
    O_CMP_GE("cmpge"),  // >=
    O_CMP_LT("cmplt"),  // <
    O_CMP_LE("cmple"),  // <=

    // math ops
    O_ADD("add"),
    O_MULT("mult"),
    O_DIV("div"),
    O_POWER("power"),
    O_MODULUS("modulus"),

}
