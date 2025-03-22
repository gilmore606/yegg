package com.dlfsystems.app

import com.dlfsystems.Yegg

object Log {

    enum class Level { DEBUG, INFO, WARN, ERR }

    fun d(m: String) { log(Level.DEBUG, m) }
    fun i(m: String) { log(Level.INFO, m) }
    fun w(m: String) { log(Level.WARN, m) }
    fun e(m: String) { log(Level.ERR, m) }

    // TODO: write actual logfile
    private fun log(level: Level, m: String) {
        if (level >= Yegg.logLevel) {
            println("$level: $m")
        }
    }

}
