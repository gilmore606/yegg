package com.dlfsystems.compiler.ast

abstract class N_EXPR: N_STATEMENT()

class N_IFELSE(val condition: N_EXPR, val eThen: N_STATEMENT, val eElse: N_STATEMENT? = null): N_EXPR()

class N_IDENTIFIER(val name: String): N_EXPR()

abstract class N_MATH_BINOP(left: N_EXPR, right: N_EXPR): N_EXPR()
class N_ADD(left: N_EXPR, right: N_EXPR): N_MATH_BINOP(left, right)
class N_SUBTRACT(left: N_EXPR, right: N_EXPR): N_MATH_BINOP(left, right)
class N_MULTIPLY(left: N_EXPR, right: N_EXPR): N_MATH_BINOP(left, right)
class N_DIVIDE(left: N_EXPR, right: N_EXPR): N_MATH_BINOP(left, right)
class N_POWER(left: N_EXPR, right: N_EXPR): N_MATH_BINOP(left, right)
class N_MODULUS(left: N_EXPR, right: N_EXPR): N_MATH_BINOP(left, right)

abstract class N_MATH_UNOP(expr: N_EXPR): N_EXPR()
class N_INVERSE(expr: N_EXPR): N_MATH_UNOP(expr)
class N_NEGATE(expr: N_EXPR): N_MATH_UNOP(expr)

abstract class N_LOGIC_BINOP(left: N_EXPR, right: N_EXPR): N_EXPR()
class N_LOGIC_AND(left: N_EXPR, right: N_EXPR): N_LOGIC_BINOP(left, right)
class N_LOGIC_OR(left: N_EXPR, right: N_EXPR): N_LOGIC_BINOP(left, right)
class N_CONDITIONAL(condition: N_EXPR, eTrue: N_EXPR, eFalse: N_EXPR): N_EXPR()

abstract class N_COMPARE_BINOP(left: N_EXPR, right: N_EXPR): N_EXPR()
class N_EQUALS(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP(left, right)
class N_NOTEQUALS(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP(left, right)
class N_GREATER_THAN(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP(left, right)
class N_LESS_THAN(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP(left, right)
class N_GREATER_EQUAL(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP(left, right)
class N_LESS_EQUAL(left: N_EXPR, right: N_EXPR): N_COMPARE_BINOP(left, right)
