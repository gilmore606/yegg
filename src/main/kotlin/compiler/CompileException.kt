package com.dlfsystems.compiler


class CompileException(m: String, lineNum: Int, charNum: Int): Exception("$m at line $lineNum c$charNum")
