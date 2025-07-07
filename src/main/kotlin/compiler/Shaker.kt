package com.dlfsystems.yegg.compiler

import com.dlfsystems.yegg.compiler.ast.expr.identifier.N_IDENTIFIER
import com.dlfsystems.yegg.compiler.ast.Node

// Assign variable IDs to identifiers.

class Shaker(val root: Node) {

    val symbols = mutableMapOf<String, Int>()

    fun shake(): Node {

        // Identify all non-variable identifiers (prop and verb refs).
        root.traverse { it.identify() }

        // Collect all unique variable names.
        root.traverse {
            if (it is N_IDENTIFIER && it.isVariable()) {
                if (!symbols.containsKey(it.name)) {
                    symbols[it.name] = symbols.size
                }
            }
        }

        // Set the variable ID for all variables.
        root.traverse {
            if (it is N_IDENTIFIER && it.isVariable()) {
                it.variableID = symbols[it.name]
            }
        }

        return root
    }

}
