package com.dlfsystems

import com.dlfsystems.server.Yegg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

abstract class YeggTest {

    companion object {
        val scope = CoroutineScope(Dispatchers.Default.limitedParallelism(1))

        @BeforeClass @JvmStatic fun setup() {
            Yegg.start(testMode = true)
        }

        @AfterClass @JvmStatic fun teardown() {
            Yegg.stop()
        }
    }

    @BeforeTest fun start() {
        Yegg.resetForTest()
    }

    protected fun yeggTest(testBlock: suspend () -> Unit) {
        runBlocking(scope.coroutineContext) {
            // Wait for Yegg to finish starting up and creating the world.
            // I'm not sure how to lock on that without making Yegg.world nullable, which would be annoying.
            delay(10L)
            testBlock()
        }
    }

    protected fun verb(traitName: String, verbName: String, source: String) {
        Yegg.world.programVerb(traitName, verbName, source)
    }

    protected suspend fun run(source: String) {
        TestConnection(scope).apply {
            start()
            runVerb(source)
            stop()
        }
    }

    protected suspend fun runForOutput(source: String, expected: String) {
        val expectedLines = expected.trimIndent().split("\n")
        TestConnection(scope).apply {
            start()
            runVerb(source)
            expectedLines.forEachIndexed { n, expectedLine ->
                assertEquals(expectedLine, output[n])
            }
            stop()
        }
    }

    protected suspend fun commandsForOutput(source: String, expected: String) {
        val commands = source.trimIndent().split("\n")
        val expectedLines = expected.trimIndent().split("\n")
        TestConnection(scope).apply {
            start()
            for (c in commands) {
                send(c)
                delay(50L)
            }
            expectedLines.forEachIndexed { n, expectedLine ->
                assertEquals(expectedLine, output[n])
            }
            stop()
        }
    }

}
