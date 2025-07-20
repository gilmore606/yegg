package com.dlfsystems.yegg.util

import com.dlfsystems.yegg.vm.VMException

// Does string match pat*tern?
fun String.matchesWildcard(pattern: String): Boolean {
    if (this == pattern) return true
    if (pattern == "*") return true
    val i = pattern.indexOf('*')
    if (i == -1) return false
    val prefix = pattern.substring(0, i)
    val suffix = pattern.substring(i + 1)
    if (this.length < prefix.length) return false
    if (this.substring(0, prefix.length) != prefix) return false
    val mySuffix = this.substring(prefix.length)
    if (mySuffix == suffix.substring(0, mySuffix.length)) return true
    return false
}

fun systemEpoch() = (System.currentTimeMillis() / 1000L).toInt()

fun fail(type: VMException.Type, m: String) { throw VMException(type, m) }

fun <T> ArrayDeque<T>.pop(): T = removeFirst()
fun <T> ArrayDeque<T>.push(element: T) = addFirst(element)

fun xColor(c: Int, m: String) = xColor(c, null, m)
fun xColor(c: Int, c2: Int? = null, m: String) =
    '\u001b' + "[38;5;$c" + 'm' + (c2?.let { '\u001b' + "[48;5;$c2" + 'm' } ?: "") + m + '\u001b' + "[0m"
fun xBold(m: String) = "\u001B[1m$m\u001B[0m"
fun xUnderline(m: String) = "\u001B[4m$m\u001B[24m"
fun String.stripAnsi() = replace("[\\u001B\\[0-9;]*m".toRegex(), "")
