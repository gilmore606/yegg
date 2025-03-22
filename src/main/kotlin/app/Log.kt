package com.dlfsystems.app

// TODO: write actual logfile, server log level, etc
object Log {

    fun i(m: String) {
        println(m)
    }

    fun w(m: String) {
        println("WARN: $m")
    }

    fun e(m: String) {
        println("ERR: $m")
    }

}
