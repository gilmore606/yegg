package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.TokenType

abstract class N_STATEMENT: Node()

class N_ASSIGN(val ident: String, val operator: TokenType, val right: N_EXPR): N_STATEMENT()
