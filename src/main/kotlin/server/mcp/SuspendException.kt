package com.dlfsystems.server.mcp

// Throw me to suspend the current task
class SuspendException(val seconds: Int): Exception()
