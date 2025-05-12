package com.dlfsystems.util

import kotlin.experimental.and
import kotlin.random.Random

object TaskIDGenerator {
    private const val ID_LENGTH = 26
    private const val ENTROPY_SIZE = 4

    // Base32 characters mapping
    private val charMapping = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k',
        'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x',
        'y', 'z'
    )

    // Generate task ID string for a given queue time.
    // Unique and lexically sortable by creation time.
    fun generate(time: Long) = generate(time, System.currentTimeMillis(), Random.nextBytes(ENTROPY_SIZE))

    private fun generate(time: Long, createTime: Long, entropy: ByteArray): String {

        val chars = CharArray(ID_LENGTH)

        // time
        chars[0] = charMapping[time.ushr(45).toInt() and 0x1f]
        chars[1] = charMapping[time.ushr(40).toInt() and 0x1f]
        chars[2] = charMapping[time.ushr(35).toInt() and 0x1f]
        chars[3] = charMapping[time.ushr(30).toInt() and 0x1f]
        chars[4] = charMapping[time.ushr(25).toInt() and 0x1f]
        chars[5] = charMapping[time.ushr(20).toInt() and 0x1f]
        chars[6] = charMapping[time.ushr(15).toInt() and 0x1f]
        chars[7] = charMapping[time.ushr(10).toInt() and 0x1f]
        chars[8] = charMapping[time.ushr(5).toInt() and 0x1f]
        chars[9] = charMapping[time.toInt() and 0x1f]

        // createTime
        chars[10] = charMapping[createTime.ushr(45).toInt() and 0x1f]
        chars[11] = charMapping[createTime.ushr(40).toInt() and 0x1f]
        chars[12] = charMapping[createTime.ushr(35).toInt() and 0x1f]
        chars[13] = charMapping[createTime.ushr(30).toInt() and 0x1f]
        chars[14] = charMapping[createTime.ushr(25).toInt() and 0x1f]
        chars[15] = charMapping[createTime.ushr(20).toInt() and 0x1f]
        chars[16] = charMapping[createTime.ushr(15).toInt() and 0x1f]
        chars[17] = charMapping[createTime.ushr(10).toInt() and 0x1f]
        chars[18] = charMapping[createTime.ushr(5).toInt() and 0x1f]
        chars[19] = charMapping[createTime.toInt() and 0x1f]

        // entropy
        chars[20] = charMapping[(entropy[0].toShort() and 0xff).toInt().ushr(3)]
        chars[21] = charMapping[(entropy[0].toInt() shl 2 or (entropy[1].toShort() and 0xff).toInt().ushr(6) and 0x1f)]
        chars[22] = charMapping[((entropy[1].toShort() and 0xff).toInt().ushr(1) and 0x1f)]
        chars[23] = charMapping[(entropy[1].toInt() shl 4 or (entropy[2].toShort() and 0xff).toInt().ushr(4) and 0x1f)]
        chars[24] = charMapping[(entropy[2].toInt() shl 5 or (entropy[3].toShort() and 0xff).toInt().ushr(7) and 0x1f)]
        chars[25] = charMapping[((entropy[3].toShort() and 0xff).toInt().ushr(2) and 0x1f)]

        return String(chars)
    }
}
