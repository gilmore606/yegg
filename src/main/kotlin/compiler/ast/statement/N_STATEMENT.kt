package com.dlfsystems.yegg.compiler.ast.statement

import com.dlfsystems.yegg.compiler.ast.Node

// A statement which doesn't return a value.

abstract class N_STATEMENT: Node() {
    override fun toText(depth: Int): String = tab(depth) + toText()
}
