package com.dlfsystems.compiler.ast.statement

import com.dlfsystems.compiler.ast.Node

// A statement which doesn't return a value.

abstract class N_STATEMENT: Node() {
    override fun toText(depth: Int): String = tab(depth) + toText()
}
