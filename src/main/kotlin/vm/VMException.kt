package com.dlfsystems.vm

class VMException(m: String, lineNum: Int, charNum: Int): Exception("$m at line $lineNum c$charNum")
