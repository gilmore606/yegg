package com.dlfsystems.compiler.ast

// A node in the syntax tree.

abstract class Node {
    var lineNum = 0
    var charNum = 0

    abstract override fun toString(): String

    open fun kids(): List<Node> = listOf()

    fun traverse(predicate: (Node)->Unit) {
        kids().forEach { it.traverse(predicate) }
        predicate(this)
    }
}
