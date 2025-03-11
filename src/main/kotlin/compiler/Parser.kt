package com.dlfsystems.compiler

import com.dlfsystems.compiler.TokenType.*
import com.dlfsystems.compiler.ast.*

// Take a stream of input tokens from Lexer and produce a tree of syntax nodes.

class Parser(inputTokens: List<Token>) {

    // Internal buffer for input tokens.  We consume these as we parse.
    private val tokens = inputTokens.toMutableList()

    // Track line and char position as we go, to tag nodes for tracebacks.
    private inline fun lineNum() = if (tokens.isEmpty()) 0 else tokens[0].lineNum
    private inline fun charNum() = if (tokens.isEmpty()) 0 else tokens[0].charNum
    private inline fun EOF() = Token(T_EOF, "", lineNum(), charNum())
    private inline fun fail(m: String) { throw CompileException(m, lineNum(), charNum()) }

    // Pull the next token from the input stream.
    private inline fun consume() = if (tokens.isEmpty()) EOF() else tokens.removeAt(0)
    // Pull the next token if of the given type, else return null.
    private inline fun consume(vararg types: TokenType) = if (nextIs(types.toList())) consume() else null

    // Peek at the next token in the input stream.
    private inline fun next(skip: Int = 0) = if (skip >= tokens.size) EOF() else tokens[skip]

    // Is the next token one of the given types?
    private inline fun nextIs(vararg types: TokenType) = (next().type in types)
    private inline fun nextIs(types: List<TokenType>) = (next().type in types)

    // Tag returned nodes with the current lineNum and charNum for tracebacks.
    private inline fun <T: Node>node(n: T): T = n.apply {
        lineNum = lineNum()
        charNum = charNum()
    }

    //
    // Parse our token stream into a tree of syntax nodes.
    // Expect a series of statements.  Return N_BLOCK containing the series of statements.
    //
    fun parse(): N_BLOCK {
        val statements = ArrayList<N_STATEMENT>()
        while(tokens.isNotEmpty()) {
            pStatement()?.also {
                statements.add(it)
            } ?: fail("incomplete statement")
        }
        return N_BLOCK(statements)
    }

    // Statements

    // Look for any statement type.
    private fun pStatement(): N_STATEMENT? {
        pBlock()?.also { return it }
        pIfThen()?.also { return it }
        pForLoop()?.also { return it }
        pReturn()?.also { return it }
        pAssign()?.also { return it }
        pExpression()?.also { return it }
        return null
    }

    // Look for: { statement statement... }
    private fun pBlock(): N_STATEMENT? {
        consume(T_BRACE_OPEN) ?: return null
        val statements = ArrayList<N_STATEMENT>()
        while (!nextIs(T_BRACE_CLOSE)) {
            pStatement()?.also { statements.add(it) } ?: fail("unclosed braces")
        }
        consume()
        return node(N_BLOCK(statements))
    }

    // Look for: if (expr) statement [else statement]
    private fun pIfThen(): N_STATEMENT? {
        consume(T_IF) ?: return null
        pExpression()?.also { condition ->
            pStatement()?.also { eThen ->
                consume(T_ELSE)?.also {
                    pStatement()?.also { eElse ->
                        return node(N_IFELSE(condition, eThen, eElse))
                    } ?: fail("missing else block")
                }
                return node(N_IFELSE(condition, eThen))
            } ?: fail("missing then block")
        } ?: fail("missing condition")
        return null
    }

    // Look for: for (assign; expr; assign) statement
    private fun pForLoop(): N_STATEMENT? {
        consume(T_FOR) ?: return null
        var withParens = true
        consume(T_PAREN_OPEN) ?: { withParens = false }
        pStatement()?.also { assign ->
            consume(T_SEMICOLON) ?: fail("expected semicolon after for-loop index assignment")
            pExpression()?.also { check ->
                consume(T_SEMICOLON) ?: fail("expected semicolon after for-loop check")
                pStatement()?.also { increment ->
                    if (withParens) consume(T_PAREN_CLOSE) ?: fail("unclosed parens around for-loop spec")
                    pStatement()?.also { body ->
                        return node(N_FORLOOP(assign, check, increment, body))
                    } ?: fail("missing for-loop body")
                } ?: fail("missing for-loop increment statement")
            } ?: fail("missing for-loop check expression")
        } ?: fail("missing for-loop init statement")
        return null
    }

    // Look for: return expr
    private fun pReturn(): N_STATEMENT? {
        consume(T_RETURN) ?: return null
        pExpression()?.also { expr ->
            return node(N_RETURN(expr))
        } ?: fail("missing return expression")
        return null
    }

    // Look for: identifier = expr
    private fun pAssign(): N_STATEMENT? {
        if (!nextIs(T_IDENTIFIER)) return null
        if (next(1).type !in listOf(T_ASSIGN, T_ADD_ASSIGN, T_SUBTRACT_ASSIGN, T_MULT_ASSIGN, T_DIV_ASSIGN)) return null
        val ident = consume()
        val operator = consume()
        pExpression()?.also { right ->
            return node(N_ASSIGN(ident.string, operator.type, right))
        } ?: fail("missing value for assignment")
        return null
    }

    // Expressions

    // Look for an expression.
    // Start with the lowest precedence operation, passing down to look for higher precedence operations.
    private fun pExpression(): N_EXPR? {
        val next = this::pAndOr
        return next()
    }

    // Look for: expr and|or expr
    private fun pAndOr(): N_EXPR? {
        val next = this::pConditional
        var left = next() ?: return null
        while (nextIs(T_AND, T_OR)) {
            val operator = consume()
            next()?.also { right ->
                left = node(if (operator.type == T_AND) N_LOGIC_AND(left, right)
                            else N_LOGIC_OR(left, right))
            }
        }
        return left
    }

    // Look for: expr ? expr : expr
    private fun pConditional(): N_EXPR? {
        val next = this::pEquals
        val left = next() ?: return null
        consume(T_QUESTION)?.also {
            next()?.also { mid ->
                consume(T_COLON)?.also {
                    next()?.also { right ->
                        return node(N_CONDITIONAL(left, mid, right))
                    } ?: fail("incomplete conditional")
                } ?: fail("missing colon in conditional")
            } ?: fail("incomplete condition")
        }
        return left
    }

    // Look for: expr ==|!= expr
    private fun pEquals(): N_EXPR? {
        val next = this::pCompare
        var left = next() ?: return null
        while (nextIs(T_EQUALS, T_NOTEQUALS)) {
            val operator = consume()
            next()?.also { right ->
                left = node(if (operator.type == T_EQUALS) N_EQUALS(left, right)
                            else N_NOTEQUALS(left, right))
            }
        }
        return left
    }

    // Look for: expr >|>=|<|<= expr
    private fun pCompare(): N_EXPR? {
        val next = this::pAdd
        var left = next() ?: return null
        while (nextIs(T_GREATER_THAN, T_GREATER_EQUAL, T_LESS_THAN, T_LESS_EQUAL)) {
            val operator = consume()
            next()?.also { right ->
                left = node(when (operator.type) {
                    T_GREATER_THAN -> N_GREATER_THAN(left, right)
                    T_GREATER_EQUAL -> N_GREATER_EQUAL(left, right)
                    T_LESS_THAN -> N_LESS_THAN(left, right)
                    else -> N_LESS_EQUAL(left, right)
                })
            }
        }
        return left
    }

    // Look for: expr +|- expr
    private fun pAdd(): N_EXPR? {
        val next = this::pPower
        var left = next() ?: return null
        while (nextIs(T_PLUS, T_MINUS)) {
            val operator = consume()
            next()?.also { right ->
                left = node(if (operator.type == T_PLUS) N_ADD(left, right)
                            else N_SUBTRACT(left, right))
            }
        }
        return left
    }

    // Look for: expr ^ expr
    private fun pPower(): N_EXPR? {
        val next = this::pMultiply
        var left = next() ?: return null
        while (nextIs(T_POWER)) {
            consume()
            next()?.also { right ->
                left = node(N_POWER(left, right))
            }
        }
        return left
    }

    // Look for: expr *|/|^ expr
    private fun pMultiply(): N_EXPR? {
        val next = this::pInverse
        var left = next() ?: return null
        while (nextIs(T_MULTIPLY, T_DIVIDE, T_MODULUS)) {
            val operator = consume()
            next()?.also { right ->
                left = node(when (operator.type) {
                    T_MULTIPLY -> N_MULTIPLY(left, right)
                    T_DIVIDE -> N_DIVIDE(left, right)
                    else -> N_MODULUS(left, right)
                })
            }
        }
        return left
    }

    // Look for: !expr | -expr
    private fun pInverse(): N_EXPR? {
        val next = this::pIfElse
        consume(T_BANG, T_MINUS)?.also { operator ->
            pExpression()?.also { right ->
                return node(if (operator.type == T_BANG) N_INVERSE(right)
                            else N_NEGATE(right))
            } ?: fail("expression expected after unary operator")
        }
        return next()
    }

    // Look for in an expr context: if expr expr else expr
    private fun pIfElse(): N_EXPR? {
        val next = this::pValue
        consume(T_IF)?.also {
            pExpression()?.also { condition ->
                pExpression()?.also { eThen ->
                    consume(T_ELSE)?.also {
                        pExpression()?.also { eElse ->
                            return node(N_IFELSE(condition, eThen, eElse))
                        } ?: fail("missing else expression")
                    } ?: fail("expected else in if expression")
                } ?: fail("missing expression")
            } ?: fail("missing condition")
        }
        return next()
    }

    // Look for a bare value (a literal, or a variable identifier)
    private fun pValue(): N_EXPR? {
        val next = this::pParens
        if (nextIs(T_STRING)) return node(N_LITERAL_STRING(consume().string))
        if (nextIs(T_INTEGER)) return node(N_LITERAL_INTEGER(consume().string.toInt()))
        if (nextIs(T_FLOAT)) return node(N_LITERAL_FLOAT(consume().string.toFloat()))
        if (nextIs(T_TRUE, T_FALSE)) return node(N_LITERAL_BOOLEAN(consume().type == T_TRUE))
        if (nextIs(T_IDENTIFIER)) return node(N_IDENTIFIER(consume().string))
        return next()
    }

    // Look for: (expr)
    private fun pParens(): N_EXPR? {
        consume(T_PAREN_OPEN)?.also {
            val expr = pExpression()
            if (expr == null) fail("incomplete expression")
            if (consume().type != T_PAREN_CLOSE) fail("unclosed parens")
            return expr
        }
        return null
    }

}
