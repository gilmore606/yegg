package com.dlfsystems.vm

// An opcode representing an instruction for a VM.

enum class Opcode(val argCount: Int = 0) {

    // Discard the stack top.
    O_DISCARD,

    // Push arg1 to stack.
    O_VAL(1),

    // Pop arg1 (as intval) values from stack, and push a list.
    O_LISTVAL(1),

    // Pop arg1 (as intval) pairs of stack values, and push a map of argX:argX+1.
    O_MAPVAL(1),

    // Push index result of pop0[pop1].
    O_INDEX,

    // Push range result of pop0[pop1..pop2].
    O_RANGE,

    // Jump to arg1 address if pop0 is false.
    O_IF(1),

    // Jump to arg1 address.
    O_JUMP(1),

    // Return from func with pop0 (or no value).
    O_RETURN,
    O_RETURNNULL,

    // Throw E_USER with pop0 as message.
    O_FAIL,

    // TODO
    O_CALL(),

    // Push variable with ID arg1.
    O_GETVAR(1), // variable ID to fetch

    // Set variable with ID arg1 to value pop0.
    O_SETVAR(1), // variable ID to store

    // Set variable with ID arg1 index pop1 to value pop0.
    O_SETVARI(1), // variable ID to store

    // Increment/decrement variable with ID arg1.
    O_INCVAR(1), // variable ID to inc
    O_DECVAR(1), // variable ID to dec

    // Push iterableSize of pop0 as VInt.
    O_ITERSIZE,

    // Push element indexed by variable arg2 of variable arg1.
    O_ITERPICK(2), // variable ID of source and index

    // Push value of property pop1 on value pop0.
    O_GETPROP,
    // Set value of property pop1 on value pop0 to value pop2.
    O_SETPROP,
    // Push trait named by string pop0.
    O_GETTRAIT,

    // Push negation of pop0.
    O_NEGATE,

    // Push the boolean result of pop0 and pop1.
    O_AND,
    O_OR,
    O_CMP_EQ,
    O_CMP_GT,
    O_CMP_GE,
    O_CMP_LT,
    O_CMP_LE,

    // Push the math result of pop0 and pop1.
    O_ADD,
    O_MULT,
    O_DIV,
    O_POWER,
    O_MODULUS,

}
