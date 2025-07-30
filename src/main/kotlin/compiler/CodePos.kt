package com.dlfsystems.yegg.compiler

import kotlinx.serialization.Serializable


@Serializable
data class CodePos(
    val l: Int,
    val c0: Int,
    val c1: Int
)
