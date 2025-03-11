package com.dlfsystems.compiler.ast

// A node in the syntax tree.

abstract class Node {
    var lineNum = 0
    var charNum = 0

    override fun toString() = toCode()
    open fun toCode(depth: Int = 0): String = toCode()
    open fun toCode(): String = "NODE"
    fun tab(depth: Int) = "  ".repeat(depth)

    open fun kids(): List<Node> = listOf()

    fun traverse(predicate: (Node)->Unit) {
        kids().forEach { it.traverse(predicate) }
        predicate(this)
    }
}
