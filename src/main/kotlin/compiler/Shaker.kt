package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.Node

// Validate the tree for semantic consistency.
// Label nodes along the way (variable IDs, etc) to assist execution.

class Shaker(val root: Node) {

    val varNameToID = mutableMapOf<String, Int>()

    fun shake(): Node {
        // Identify all variables.
        // Check for assignment before reference.
        root.traverse { node ->

        }

        return root
    }
}
