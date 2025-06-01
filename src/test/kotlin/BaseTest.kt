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
    fun `Run a bunch of fiddly code`() = runBlocking {
        val source = $$"""
            foo = 18
            bar = 15.0
            baz = ["beef", "pork", "cheese"]
            if (baz == bar) fail "ACK!"
            for (x in baz) {
                notifyConn("$x is $foo $bar ${bar * foo}")
                bar += 0.7
                foo = foo / 2
            }
            fooMap = ["rat": 12, "fox": 3, "otter": 9]
            notifyConn("${fooMap.keys}")
            for (i=0;i<fooMap.keys.length;i++) {
                notifyConn("run $i")
                j = 0
                poo = 1
                animal = fooMap.keys[i]
                while (j < fooMap[fooMap.keys[i]]) {
                    poo++
                    j++
                }
                notifyConn("$animal $poo")
            }
            notifyConn("All done.")
        """
        val output = runAsVerb(source)
        assertContains(output, "beef is 18 15.0 270.0")
        assertContains(output, "pork is 9 15.7 141.3")
        assertContains(output, "cheese is 4 16.4 65.6")
        assertContains(output, "\"rat\", \"fox\", \"otter\"")
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
