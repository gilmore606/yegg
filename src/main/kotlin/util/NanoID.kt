package com.dlfsystems.util

import java.security.SecureRandom

object NanoID {

    const val SIZE = 8

    const val CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    // Calculated from SIZE and CHARS.size.
    private const val STEP = 10
    private const val MASK = 63

    private val random = SecureRandom()

    fun newID(): String {
        val id = StringBuilder(SIZE)
        val bytes = ByteArray(STEP)
        while (true) {
            random.nextBytes(bytes)
            for (i in 0 until STEP) {
                val ci = bytes[i].toInt() and MASK
                if (ci < CHARS.length) {
                    id.append(CHARS[ci])
                    if (id.length == SIZE) {
                        return id.toString()
                    }
                }
            }
        }
    }

}
