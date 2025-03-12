package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import java.util.UUID

// A node in the syntax tree.

abstract class Node {
    val id = UUID.randomUUID()

    var lineNum = 0
    var charNum = 0

    override fun toString() = toText()
    open fun toText(depth: Int = 0): String = toText()
    open fun toText(): String = "NODE"
    fun tab(depth: Int) = "  ".repeat(depth)

    open fun kids(): List<Node> = listOf()

    fun traverse(predicate: (Node)->Unit) {
        kids().forEach { it.traverse(predicate) }
        predicate(this)
    }

    open fun code(coder: Coder) { }
}
