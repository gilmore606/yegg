package com.dlfsystems.yegg.compiler.parser

import com.dlfsystems.yegg.compiler.CodePos


// A token lexed from a verb source string, with its source string and line/char position for tracebacks.
data class Token(
    val type: Type,
    val string: String,
    val pos: CodePos
) {
    enum class Type(val literal: String, val isKeyword: Boolean = false) {
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
        T_ARROW("->"),
        T_TICK("'"),
        T_BACKTICK("`"),

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
        T_ELSE("else", true),
        T_FALSE("false", true),
        T_FOR("for", true),
        T_IF("if", true),
        T_IN("in", true),
        T_RETURN("return", true),
        T_TRUE("true", true),
        T_WHILE("while", true),
        T_THROW("throw", true),
        T_WHEN("when", true),
        T_SUSPEND("suspend", true),
        T_FORK("fork", true),
        T_BREAK("break", true),
        T_CONTINUE("continue", true),
        T_TRY("try", true),
        T_CATCH("catch", true),

        T_EOF("EOF");
    }

    override fun toString() = when (type) {
        Type.T_STRING -> "STRING(\"$string\")"
        Type.T_STRING_SUB_START -> "T_STRING_SUB_START(\"$string\")"
        Type.T_INTEGER, Type.T_FLOAT -> "NUM($string)"
        Type.T_IDENTIFIER -> "IDENT($string)"
        else -> type.toString()
    }
}
