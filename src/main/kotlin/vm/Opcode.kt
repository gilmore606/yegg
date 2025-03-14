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
    O_FETCHVAR("fetchVar", 1),
    O_STOREVAR("storeVar", 1),
    O_INCVAR("incVar", 1),
    O_DECVAR("decVar", 1),

    // property ops
    O_FETCHPROP("fetchProp"),
    O_STOREPROP("storeProp"),
    O_FETCHTRAIT("fetchTrait"),

    // boolean ops
    O_NEGATE("negate"),
    O_AND("and"),       // &&
    O_OR("or"),         // ||
    O_CMP_EQ("cmpEq"),  // ==
    O_CMP_GT("cmpGt"),  // >
    O_CMP_GE("cmpGe"),  // >=
    O_CMP_LT("cmpLt"),  // <
    O_CMP_LE("cmpLe"),  // <=

    // math ops
    O_ADD("add"),
    O_MULT("mult"),
    O_DIV("div"),
    O_POWER("power"),
    O_MODULUS("modulus"),

}
