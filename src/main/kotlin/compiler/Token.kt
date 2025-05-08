package com.dlfsystems.compiler

enum class TokenType(val literal: String, val isKeyword: Boolean = false) {
    // Groupers
    T_PAREN_OPEN("("),
    T_PAREN_CLOSE(")"),
    T_BRACE_OPEN("{"),
    T_BRACE_CLOSE("}"),
    T_BRACKET_OPEN("["),
    T_BRACKET_CLOSE("]"),

    // Operators
    T_ASSIGN("="),
    T_EQUALS("=="),
    T_NOTEQUALS("!="),
    T_GREATER_THAN(">"),
    T_LESS_THAN("<"),
    T_GREATER_EQUAL(">="),
    T_LESS_EQUAL("<="),
    T_PLUS("+"),
    T_MINUS("-"),
    T_ADD_ASSIGN("+="),
    T_SUBTRACT_ASSIGN("-="),
    T_MULT_ASSIGN("*="),
    T_DIV_ASSIGN("/="),
    T_INCREMENT("++"),
    T_DECREMENT("--"),
    T_MULTIPLY("*"),
    T_DIVIDE("/"),
    T_POWER("^"),
    T_MODULUS("%"),
    T_DOT("."),
    T_DOTDOT(".."),
    T_COLON(":"),
    T_SEMICOLON(";"),
    T_DOLLAR("$"),
    T_BANG("!"),
    T_QUESTION("?"),
    T_LOGIC_OR("||"),
    T_LOGIC_AND("&&"),
    T_COMMA(","),
    T_AT("@"),
    T_ARROW("->"),

    // Literals
    T_IDENTIFIER("ident"),
    T_COMMENT("//"),
    T_STRING("\"\""),
    T_STRING_SUB_START("\"{"),
    T_STRING_SUB_END("}"),
    T_INTEGER("n"),
    T_FLOAT("n.n"),
    T_OBJREF("#xxxxx"),

    // Keywords
    T_AND("and", true),
    T_ELSE("else", true),
    T_FALSE("false", true),
    T_FOR("for", true),
    T_IF("if", true),
    T_IN("in", true),
    T_NULL("null", true),
    T_OR("or", true),
    T_RETURN("return", true),
    T_TRUE("true", true),
    T_WHILE("while", true),
    T_FAIL("fail", true),
    T_WHEN("when", true),

    T_EOF("EOF");
}

// A token lexed from a verb source string, with its source string and line/char position for tracebacks.
data class Token(
    val type: TokenType,
    val string: String,
    val lineNum: Int,
    val charNum: Int,
) {
    override fun toString() = when (type) {
        TokenType.T_STRING -> "STRING(\"$string\")"
        TokenType.T_STRING_SUB_START -> "T_STRING_SUB_START(\"$string\")"
        TokenType.T_INTEGER, TokenType.T_FLOAT -> "NUM($string)"
        TokenType.T_IDENTIFIER -> "IDENT($string)"
        else -> type.toString()
    }
}
