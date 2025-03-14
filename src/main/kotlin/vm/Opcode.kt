package com.dlfsystems.vm

// An opcode representing an instruction for a VM.

enum class Opcode(val argCount: Int = 0) {

    // stack ops
    O_DISCARD,
    O_LITERAL(1),

    // flow ops
    O_IF(1),
    O_JUMP(1),
    O_RETURN,

    // variable ops
    O_FETCHVAR(1),
    O_STOREVAR(1),
    O_INCVAR(1),
    O_DECVAR(1),

    // property ops
    O_FETCHPROP,
    O_STOREPROP,
    O_FETCHTRAIT,

    // boolean ops
    O_NEGATE,
    O_AND,
    O_OR,
    O_CMP_EQ,
    O_CMP_GT,
    O_CMP_GE,
    O_CMP_LT,
    O_CMP_LE,

    // math ops
    O_ADD,
    O_MULT,
    O_DIV,
    O_POWER,
    O_MODULUS,

}
