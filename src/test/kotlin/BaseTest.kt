package com.dlfsystems

import com.dlfsystems.server.TestConnection
import com.dlfsystems.server.Yegg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import kotlin.test.Test
import kotlin.test.assertContains

class BaseTest {

    companion object {
        val scope = CoroutineScope(Dispatchers.IO)

        @BeforeClass @JvmStatic fun setup() {
            Yegg.start(testMode = true)
        }

        @AfterClass @JvmStatic fun teardown() {
            Yegg.stop()
        }
    }


    @Test
    fun `variables`() = runBlocking {
        val source = """
            foo = 1
            foo = 2
            notifyConn(foo)
        """
        val output = runAsVerb(source)
        assertContains(output, "2")
    }

    @Test
    fun `math`() {
        val source = """
            [a1, a2] = args
            return a1 * a2
        """
    }


    private suspend fun runAsVerb(source: String): String {
        val conn = TestConnection(scope)
        conn.start()
        conn.runVerb(source)
        conn.stop()
        return conn.output
    }

}
