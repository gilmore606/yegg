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
    // Are the next tokens each of the given ordered types?
    private inline fun nextAre(vararg types: TokenType): Boolean {
        for (i in 0 until types.size) {
            if (next(i).type != types[i]) return false
        }
        return true
    }

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

    // Parse any statement type we find next.
    private fun pStatement(): N_STATEMENT? {
        pBlock()?.also { return it }
        pIfThen()?.also { return it }
        pForLoop()?.also { return it }
        pWhileLoop()?.also { return it }
        pReturn()?.also { return it }
        pIncrement()?.also { return it }
        pAssign()?.also { return it }
        pExprStatement()?.also { return it }
        return null
    }

    // Parse: { <statement> <statement>... }
    private fun pBlock(): N_STATEMENT? {
        consume(T_BRACE_OPEN) ?: return null
        val statements = ArrayList<N_STATEMENT>()
        while (!nextIs(T_BRACE_CLOSE)) {
            pStatement()?.also { statements.add(it) } ?: fail("non-statement in braces")
        }
        consume(T_BRACE_CLOSE) ?: fail("unclosed braces")
        return when (statements.size) {
            0 -> null
            1 -> node(statements[0])
            else -> node(N_BLOCK(statements))
        }
    }

    // Parse: if <expr> <statement> [else <statement>]
    private fun pIfThen(): N_STATEMENT? {
        consume(T_IF) ?: return null
        pExpression()?.also { condition ->
            pStatement()?.also { eThen ->
                consume(T_ELSE)?.also {
                    pStatement()?.also { eElse ->
                        return node(N_IFSTATEMENT(condition, eThen, eElse))
                    } ?: fail("missing else block")
                }
                return node(N_IFSTATEMENT(condition, eThen))
            } ?: fail("missing then block")
        } ?: fail("missing condition")
        return null
    }

    // Parse: for <init>;<check>;<increment> <statement>
    private fun pForLoop(): N_STATEMENT? {
        consume(T_FOR) ?: return null
        val withParen = consume(T_PAREN_OPEN)
        pStatement()?.also { assign ->
            consume(T_SEMICOLON) ?: fail("expected semicolon after for-loop index assignment")
            pExpression()?.also { check ->
                consume(T_SEMICOLON) ?: fail("expected semicolon after for-loop check")
                pStatement()?.also { increment ->
                    withParen?.also { consume(T_PAREN_CLOSE) ?: fail("unclosed parens around for-loop spec") }
                    pStatement()?.also { body ->
                        return node(N_FORLOOP(assign, check, increment, body))
                    } ?: fail("missing for-loop body")
                } ?: fail("missing for-loop increment statement")
            } ?: fail("missing for-loop check expression")
        } ?: fail("missing for-loop init statement")
        return null
    }

    // Parse: while <expr> <statement>
    private fun pWhileLoop(): N_STATEMENT? {
        consume(T_WHILE) ?: return null
        pExpression()?.also { check ->
            pStatement()?.also { body ->
                return node(N_WHILELOOP(check, body))
            } ?: fail("missing while body")
        } ?: fail("missing while check expression")
        return null
    }

    // Parse: return [<expr>]
    private fun pReturn(): N_STATEMENT? {
        consume(T_RETURN) ?: return null
        return node(N_RETURN(pExpression()))
    }

    // Parse: <ident>++|-- / ++|--<ident>
    private fun pIncrement(): N_STATEMENT? {
        fun make(ident: String, isDec: Boolean): N_STATEMENT {
            val identifier = node(N_IDENTIFIER(ident))
            return node(N_INCREMENT(identifier, isDec))
        }
        consume(T_INCREMENT, T_DECREMENT)?.also { operator ->
            consume(T_IDENTIFIER)?.also { ident ->
                return make(ident.string, operator.type == T_DECREMENT)
            } ?: fail("increment missing identifier")
        }
        if (nextIs(T_IDENTIFIER) && next(1).type in listOf(T_INCREMENT, T_DECREMENT)) {
            return make(consume().string, consume().type == T_DECREMENT)
        }
        return null
    }

    // Parse: <expr> = <expr>
    // This should run last in statement parsing, since it consumes expressions.
    private fun pAssign(): N_STATEMENT? {
        pExpression()?.also { left ->
            if (nextIs(T_ASSIGN, T_ADD_ASSIGN, T_SUBTRACT_ASSIGN, T_MULT_ASSIGN, T_DIV_ASSIGN)) {
                val operator = consume()
                pExpression()?.also { right ->
                    return node(when (operator.type) {
                        T_ADD_ASSIGN -> N_ASSIGN(left, node(N_ADD(left, right)))
                        T_SUBTRACT_ASSIGN -> N_ASSIGN(left, node(N_SUBTRACT(left, right)))
                        T_MULT_ASSIGN -> N_ASSIGN(left, node(N_MULTIPLY(left, right)))
                        T_DIV_ASSIGN -> N_ASSIGN(left, node(N_DIVIDE(left, right)))
                        else -> N_ASSIGN(left, right)
                    })
                } ?: fail("missing predicate for assignment")
            }
            return node(N_EXPRSTATEMENT(left))
        }
        return null
    }

    // Parse a bare expression whose return value is ignored.
    private fun pExprStatement(): N_STATEMENT? {
        pExpression()?.also { expr ->
            return node(N_EXPRSTATEMENT(expr))
        }
        return null
    }

    // Expressions

    // Parse any expression we find next.
    // Start with the lowest precedence operation, passing down to look for higher precedence operations.
    private fun pExpression(): N_EXPR? {
        val next = this::pAndOr
        return next()
    }

    // Parse: <expr> and|or <expr>
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

    // Parse: <expr> ? <expr> : <expr>
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

    // Parse: <expr> ==|!= <expr>
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

    // Parse: <expr> >|>=|<|<= <expr>
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

    // Parse: <expr> +|- <expr>
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

    // Parse: <expr> ^ <expr>
    private fun pPower(): N_EXPR? {
        val next = this::pMultiply
        var left = next() ?: return null
        consume(T_POWER)?.also {
            next()?.also { right ->
                left = node(N_POWER(left, right))
            }
        }
        return left
    }

    // Parse: <expr> *|/|^ <expr>
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

    // Parse: !|-<expr>
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

    // Parse (as expression): if <expr> <expr> else <expr>
    private fun pIfElse(): N_EXPR? {
        val next = this::pFuncall
        consume(T_IF)?.also {
            pExpression()?.also { condition ->
                pExpression()?.also { eThen ->
                    consume(T_ELSE)?.also {
                        pExpression()?.also { eElse ->
                            return node(N_CONDITIONAL(condition, eThen, eElse))
                        } ?: fail("missing else expression")
                    } ?: fail("expected else in if expression")
                } ?: fail("missing expression")
            } ?: fail("missing condition")
        }
        return next()
    }

    // Parse a function call: <expr>(<expr>, ...)
    private fun pFuncall(): N_EXPR? {
        val next = this::pDotref
        val left = next() ?: return null
        consume(T_PAREN_OPEN)?.also {
            val args = mutableListOf<N_EXPR>()
            var moreArgs = true
            while (moreArgs) {
                pExpression()?.also { arg ->
                    args.add(arg)
                    consume(T_COMMA) ?: run { moreArgs = false }
                } ?: run { moreArgs = false }
            }
            consume(T_PAREN_CLOSE) ?: fail("unclosed parens after function args")
            return node(N_FUNCREF(left, args))
        }
        return left
    }

    // Parse a prop or func ref: <expr>.<expr>
    private fun pDotref(): N_EXPR? {
        val next = this::pIndex
        var left = next() ?: return null
        while (nextIs(T_DOT)) {
            consume(T_DOT)
            next()?.also { right ->
                left = node(N_PROPREF(left, right))
            } ?: fail("expression expected after dot reference")
        }
        return left
    }

    // Parse an index ref: <expr>[<expr>]
    private fun pIndex(): N_EXPR? {
        val next = this::pTrait
        var left = next() ?: return null
        while (nextIs(T_BRACKET_OPEN)) {
            consume(T_BRACKET_OPEN)
            pExpression()?.also { index ->
                left = node(N_INDEXREF(left, index))
            } ?: fail("expression expected for index reference")
            consume(T_BRACKET_CLOSE) ?: fail("missing close bracket for index reference")
        }
        return left
    }

    // Parse a trait reference: $<expr>
    private fun pTrait(): N_EXPR? {
        val next = this::pValue
        consume(T_DOLLAR)?.also {
            next()?.also { expr ->
                return node(N_TRAITREF(expr))
            } ?: fail("expected expression after $")
        }
        return next()
    }

    // Parse a bare value (a literal, or a variable identifier)
    private fun pValue(): N_EXPR? {
        val next = this::pParens
        consume(T_STRING)?.also { return node(N_LITERAL_STRING(it.string)) }
        consume(T_INTEGER)?.also { return node(N_LITERAL_INTEGER(it.string.toInt())) }
        consume(T_FLOAT)?.also { return node(N_LITERAL_FLOAT(it.string.toFloat())) }
        consume(T_IDENTIFIER)?.also { return node(N_IDENTIFIER(it.string)) }
        if (nextIs(T_TRUE, T_FALSE)) return node(N_LITERAL_BOOLEAN(consume().type == T_TRUE))
        return next()
    }

    // Parse: (<expr>)
    private fun pParens(): N_EXPR? {
        consume(T_PAREN_OPEN)?.also {
            pExpression()?.also { expr ->
                consume(T_PAREN_CLOSE) ?: fail("unclosed parens")
                return node(N_PARENS(expr))
            } ?: fail("non-expressions in parens")
        }
        return null
    }

}
