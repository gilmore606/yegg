package com.dlfsystems.vm

// An opcode representing an instruction for a VM.

enum class Opcode(val argCount: Int = 0) {

    // stack ops
    O_DISCARD,

    // value ops
    O_VAL(1), // value to push
    O_LISTVAL(1), // number of values (from stack) for pushed list value
    O_MAPVAL(1), // number of pairs (from stack) for pushed map value
    O_INDEX,
    O_RANGE,

    // flow ops
    O_IF(1), // address to jump if false
    O_JUMP(1),  // address to jump
    O_RETURN,

    // func ops
    O_CALL(),

    // variable ops
    O_GETVAR(1), // variable ID to fetch
    O_SETVAR(1), // variable ID to store
    O_SETVARI(1), // variable ID to store
    O_INCVAR(1), // variable ID to inc
    O_DECVAR(1), // variable ID to dec

    // property ops
    O_GETPROP,
    O_SETPROP,
    O_GETTRAIT,

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
