package com.dlfsystems

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class LanguageTest: YeggTest() {

    @Test
    fun `Math operator precedence`() = runBlocking {
        runForOutput($$"""
            notifyConn(14 * 10 + 71 / 3 ^ 3)
            notifyConn(500 - 100 / 2 * 5)
        """, """
            142
            250
        """)
    }

    @Test
    fun `Variables`() = runBlocking {
        runForOutput($$"""
            foo = 27
            foo = 34
            notifyConn("foo is $foo")
            bar = foo
            foo = 13
            notifyConn("bar is $bar")
        """, """
            foo is 34
            bar is 34
        """)
    }

    @Test
    fun `Run a bunch of fiddly code`() = runBlocking {
        runForOutput($$"""
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
            for (i=0;i<fooMap.keys.size;i++) {
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
        """, """
            beef is 18 15.0 270.0
            pork is 9 15.7 141.3
            cheese is 4 16.4 65.6
            "rat", "fox", "otter"
            rat 13
            fox 4
            otter 10
            All done.
        """)
    }

}
