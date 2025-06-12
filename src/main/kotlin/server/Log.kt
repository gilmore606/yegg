package com.dlfsystems.server

import java.io.BufferedWriter
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Log {

    enum class Level(val disp: String) { DEBUG("DEBUG"), INFO("INFO "), WARN("WARN "), ERROR("ERROR") }

    private val timestamp: String
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))

    private var writer: BufferedWriter? = null

    fun d(tag: String, m: String) { log(Level.DEBUG, tag, m) }
    fun i(tag: String, m: String) { log(Level.INFO, tag, m) }
    fun w(tag: String, m: String) { log(Level.WARN, tag, m) }
    fun e(tag: String, m: String) { log(Level.ERROR, tag, m) }

    fun start(filename: String) {
        File("$filename.log.old").delete()
        File("$filename.log").renameTo(File("$filename.log.old"))
        writer = File("$filename.log").bufferedWriter()
    }

    fun flush() {
        writer?.flush()
    }

    fun stop() {
        writer?.flush()
        writer?.close()
    }

    private fun log(level: Level, tag: String, m: String) {
        if (level >= Yegg.conf.logLevel) {
            val line = "${level.disp}: [$tag] $m"
            if (Yegg.conf.logToConsole) println(line)
            writer?.write("$line\n")
        }
    }

}
