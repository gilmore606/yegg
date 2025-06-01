package com.dlfsystems

import com.dlfsystems.server.TestConnection
import com.dlfsystems.server.Yegg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.AfterClass
import org.junit.BeforeClass
import kotlin.test.assertEquals

open class YeggTest {

    companion object {
        val scope = CoroutineScope(Dispatchers.IO)

        @BeforeClass @JvmStatic fun setup() {
            Yegg.start(testMode = true)
        }

        @AfterClass @JvmStatic fun teardown() {
            Yegg.stop()
        }
    }

    protected suspend fun runForOutput(source: String, expected: String) {
        val conn = TestConnection(scope)
        conn.start()
        conn.runVerb(source.trimIndent())

        val expectedLines = expected.trimIndent().split("\n")
        expectedLines.forEachIndexed { n, expectedLine ->
            assertEquals(expectedLine, conn.output[n])
        }

        conn.stop()
    }

}
