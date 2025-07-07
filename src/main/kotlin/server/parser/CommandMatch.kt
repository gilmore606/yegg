package com.dlfsystems.yegg.server.parser

import com.dlfsystems.yegg.value.Value
import com.dlfsystems.yegg.world.Obj
import com.dlfsystems.yegg.world.trait.Trait

// The successful result of matching a Command, which contains the info needed to execute it.

class CommandMatch(
    // The verb to execute.
    val verb: String,
    // The trait which holds the verb to execute.
    val trait: Trait,
    // The 'this' object for creating the Context to execute in.
    var obj: Obj?,
    // Args to pass to the verb.
    val args: List<Value>,
)
