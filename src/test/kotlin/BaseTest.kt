package com.dlfsystems

import com.dlfsystems.server.Connection
import com.dlfsystems.server.Yegg
import com.dlfsystems.server.onYeggThread
import com.dlfsystems.value.Value
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.AfterClass
import org.junit.BeforeClass
import kotlin.test.Test
import kotlin.test.assertContains

class BaseTest {

    companion object {
        val scope = TestScope()

        @BeforeClass @JvmStatic fun setup() {
            Yegg.start(testMode = true)
        }

        @AfterClass @JvmStatic fun teardown() {
            Yegg.stop()
        }
    }


    @Test
    fun `variables`() = runTest {
            val source = """
                foo = 1
                foo = 2
                notifyConn(foo)
            """
            assertContains(runYegg(source), "2")
        }

    @Test
    fun `math`() {
        val source = """
            [a1, a2] = args
            return a1 * a2
        """
    }


    private suspend fun runYegg(source: String, args: List<Value> = listOf()): String {
        var output = ""
        val conn = Connection { scope.launch { output += "$it\n" } }
        onYeggThread { conn.sendText(";;$source") }
        delay(100L)
        return output
    }

}
