package com.dlfsystems.util

import java.security.SecureRandom

object NanoID {

    const val CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun newID(size: Int = 8, step: Int = 16, mask: Int = 61): String {
        val random = SecureRandom()
        val idBuilder = StringBuilder(size)
        val bytes = ByteArray(step)
        while (true) {
            random.nextBytes(bytes)
            for (i in 0 until step) {
                val cIndex = bytes[i].toInt() and mask
                if (cIndex < CHARS.length) {
                    idBuilder.append(CHARS[cIndex])
                    if (idBuilder.length == size) {
                        return idBuilder.toString()
                    }
                }
            }
        }
    }

}
