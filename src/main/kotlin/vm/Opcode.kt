package com.dlfsystems.yegg.vm

// An opcode representing an instruction for a VM.
// pop0/pop1 = values popped from the stack.
// arg1/arg2 = non-opcode VMwords written directly after the opcode.

enum class Opcode(val argCount: Int = 0) {

    // Discard the stack top.
    O_DISCARD,

    // Push arg1 to stack.
    O_VAL(1),

    // Pop arg1 (as intval) values from stack, and push a list.
    O_LISTVAL(1),

    // Pop arg1 (as intval) pairs of stack values, and push a map of argX:argX+1.
    O_MAPVAL(1),

    // pop0 = VList of string arg names
    // pop1 = VList of string scope var names
    // arg1 = blockID in current executable
    // Aggregate these to create and push a VFun.
    O_FUNVAL(1),

    // pop0 = message expression
    // arg1 = un-messaged VErr
    O_ERRVAL(1),

    // Push index result of pop0[pop1].
    O_GETI,

    // Push range result of pop0[pop1..pop2].
    O_GETRANGE,

    // Jump to arg1 address if pop0 is false.
    O_IF(1),

    // Jump to arg1 address.
    O_JUMP(1),

    // Return from verb with pop0 (or no value).
    O_RETURN,
    O_RETURNNULL,

    // Throw E_USER with pop0 as message.
    O_THROW,

    // Suspend for pop0 seconds.
    O_SUSPEND,

    // Fork pop0 VFun pop1 seconds in the future.
    // Push VTask with taskID of forked task.
    O_FORK,

    // Call verb with arg1 stack args.
    O_CALL(1),

    // Call fun with arg1 name and arg2 stack args.
    // Push result.
    O_FUNCALL(2),

    // Call super.verb() with arg1 stack args.
    // Push result.
    O_PASS(1),

    // Suspend task and wait for a line (or lines) of input.
    O_READLINE,
    O_READLINES,

    // Begin try for arg1 count of VErr on stack (or all err if arg1=0)
    // Pass thrown err as variableID arg2 (or 'it' if -1) to catch handler at addr arg3
    O_TRY(3),
    O_TRYEND,

    // Push variable with ID arg1.
    O_GETVAR(1), // variable ID to fetch

    // Set variable with ID arg1 to value pop0.
    O_SETVAR(1), // variable ID to store

    // Increment/decrement variable with ID arg1.
    O_INCVAR(1), // variable ID to inc
    O_DECVAR(1), // variable ID to dec

    // Set index pop1 of value pop0.
    O_SETI,

    // Set range pop2..pop1 of value pop0.
    O_SETRANGE,

    // Assign pop1 list's elements to pop2 list of varnames.
    O_DESTRUCT,

    // Push iterableSize of pop0 as VInt.
    O_ITERSIZE,

    // Push element indexed by variable arg2 of variable arg1.
    O_ITERPICK(2), // variable ID of source and index

    // Push value of property pop1 on value pop0.
    O_GETPROP,

    // Set value of property pop1 on value pop0 to value pop2.
    O_SETPROP,

    // Push trait named by string pop0.
    O_TRAIT,

    // Push negation of pop0.
    O_NEGATE,

    // Push the boolean result of pop0 and pop1.
    O_IN,
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


    // Optimizer instructions

    // SETVAR then GETVAR.
    O_SETGETVAR(1),

    // Compare pop0 to zero.
    O_CMP_EQZ,
    O_CMP_NEZ,
    O_CMP_GTZ,
    O_CMP_GEZ,
    O_CMP_LTZ,
    O_CMP_LEZ,

    // Jump to arg2 address if var arg1 == pop0.
    O_IFVAREQ(2),

    // Push the addition of pop0 and value arg1.
    O_ADDVAL(1),

    // Push the addition of pop0, pop1, and value arg1.
    O_CONCAT(1),

    // Call fun but do not push result.
    O_FUNCALLST(2),

    // Call super but do not push result.
    O_PASSST(1),

    // Call literal-named verb but do not push result.
    O_VCALLST(2),

    // Call literal-named verb.
    O_VCALL(2),

    // Get literal-named property.
    O_VGETPROP(1),

    // Get literal-named trait.
    O_VTRAIT(1),

    // Return arg1 value.
    O_RETVAL(1),

    // Return variable value with arg1 varID.
    O_RETVAR(1),

}
