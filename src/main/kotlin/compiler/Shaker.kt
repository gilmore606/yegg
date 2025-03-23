package com.dlfsystems.compiler

import com.dlfsystems.compiler.ast.N_IDENTIFIER
import com.dlfsystems.compiler.ast.Node

// Assign variable IDs to identifiers.

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
