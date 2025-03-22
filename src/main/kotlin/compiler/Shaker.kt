package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.N_IDENTIFIER
import com.dlfsystems.compiler.ast.Node

// Validate the tree for semantic consistency.
// Label nodes along the way (variable IDs, etc) to assist execution.

class Shaker(val root: Node) {

    val variableIDs = mutableMapOf<String, Int>()

    fun shake(): Node {

        // Identify all non-variable identifiers (prop and verb refs).
        root.traverse { it.identify() }

        // Collect all unique variable names.
        root.traverse {
            if (it is N_IDENTIFIER && it.isVariable()) {
                if (!variableIDs.containsKey(it.name)) {
                    variableIDs[it.name] = variableIDs.size
                }
            }
        }

        // Set the variable ID for all variables.
        root.traverse {
            if (it is N_IDENTIFIER && it.isVariable()) {
                it.variableID = variableIDs[it.name]
            }
        }

        return root
    }
}
