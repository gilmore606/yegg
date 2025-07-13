package com.dlfsystems.yegg.compiler.ast

import com.dlfsystems.yegg.compiler.Coder
import com.dlfsystems.yegg.compiler.CompileException
import com.dlfsystems.yegg.util.NanoID

// A node in the syntax tree.

@JvmInline value class NodeID(val id: String) { override fun toString() = id }

abstract class Node {
    val id: NodeID = NodeID(makeID())

    var lineNum = 0
    var charNum = 0

    fun fail(m: String) { throw CompileException(m, lineNum, charNum)}

    open fun kids(): List<Node> = listOf()

    // Run predicate on this node and all descendants.
    fun traverse(predicate: (Node)->Unit) {
        kids().forEach { it.traverse(predicate) }
        predicate(this)
    }

    // Identify the type of any identifiers we point to.
    open fun identify() { }

    // Collect all variable names under this node.
    fun collectVars(): List<String> = buildSet {
        kids().forEach { addAll(it.collectVars()) }
        variableName()?.also { add(it) }
    }.toList()

    // Return our name if we're an identifier for a variable.
    open fun variableName(): String? = null

    // Generate opcodes for this node.  Do not call directly; use Coder.code(Node).
    open fun code(c: Coder) { }

    fun makeID() = NanoID.newID()
}
