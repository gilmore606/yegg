package com.dlfsystems.compiler.ast

// A node in the syntax tree.

open class Node {
    var lineNum = 0
    var charNum = 0

    open fun kids() = listOf<Node>()
}
