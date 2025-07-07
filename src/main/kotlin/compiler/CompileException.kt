package com.dlfsystems.yegg.compiler

import com.dlfsystems.yegg.compiler.ast.Node
import com.dlfsystems.yegg.compiler.parser.Token
import com.dlfsystems.yegg.vm.VMWord

class CompileException(m: String, lineNum: Int, charNum: Int): Exception("$m at line $lineNum c$charNum") {
    var code: List<VMWord>? = null
    var symbols: Map<String, Int>? = null
    var tokens: List<Token>? = null
    var ast: Node? = null

    fun withInfo(c: List<VMWord>?, vids: Map<String, Int>?, t: List<Token>?, a: Node?): CompileException {
        code = c
        symbols = vids
        tokens = t
        ast = a
        return this
    }
}
