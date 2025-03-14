package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.CompileException
import java.util.UUID

// A node in the syntax tree.

abstract class Node {
    val id = UUID.randomUUID()

    var lineNum = 0
    var charNum = 0

    // TODO: get rid of this failed attempt at tabbed output
    override fun toString() = toText()
    open fun toText(depth: Int = 0): String = toText()
    open fun toText(): String = "NODE"
    private fun tab(depth: Int) = "  ".repeat(depth)
    private fun fail(m: String) { throw CompileException(m, lineNum, charNum)}

    open fun kids(): List<Node> = listOf()

    fun traverse(predicate: (Node)->Unit) {
        kids().forEach { it.traverse(predicate) }
        predicate(this)
    }

    // Identify the type of any identifiers we point to.
    open fun identify() { }

    // Generate opcodes for this node.
    open fun code(code: Coder) { }
}
