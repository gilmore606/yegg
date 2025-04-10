package com.dlfsystems.compiler.ast

import com.dlfsystems.compiler.Coder
import com.dlfsystems.compiler.CompileException
import io.viascom.nanoid.NanoId

// A node in the syntax tree.

data class NodeID(val id: String) { override fun toString() = id }

abstract class Node {
    val id: NodeID = makeID()

    var lineNum = 0
    var charNum = 0

    // TODO: get rid of this failed attempt at tabbed output
    override fun toString() = toText()
    open fun toText(depth: Int = 0): String = toText()
    open fun toText(): String = "NODE"
    fun tab(depth: Int) = "  ".repeat(depth)
    fun fail(m: String) { throw CompileException(m, lineNum, charNum)}

    open fun kids(): List<Node> = listOf()

    fun traverse(predicate: (Node)->Unit) {
        kids().forEach { it.traverse(predicate) }
        predicate(this)
    }

    // Identify the type of any identifiers we point to.
    open fun identify() { }

    // Generate opcodes for this node.
    open fun code(code: Coder) { }

    companion object {
        fun makeID() = NodeID(NanoId.generateOptimized(8, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", 61, 16))
    }
}
