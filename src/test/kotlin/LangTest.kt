package com.dlfsystems

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class LangTest: YeggTest() {

    @Test
    fun `Math`() = runBlocking {
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
    fun `Lists`() = runBlocking {
        runForOutput($$"""
            foo = ["beef", "pork", "cheese"]
            notifyConn(foo[1..2])
            bar = ["eggs", "milk"]
            notifyConn(foo + bar)
            baz = [foo[1], bar[1]]
            notifyConn(baz)
            baz.setAddAll(["milk", "pee"])
            notifyConn(baz)
            baz.push("poo")
            if (baz.first == "poo") notifyConn("poohead")
        ""","""
            "pork", "cheese"
            "beef", "pork", "cheese", "eggs", "milk"
            "pork", "milk"
            "pork", "milk", "pee"
            poohead
        """)
    }

    @Test
    fun `Maps`() = runBlocking {
        runForOutput($$"""
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
        """, """
            "rat", "fox", "otter"
            rat 13
            fox 4
            otter 10
        """)
    }

    @Test
    fun `For and while loops`() = runBlocking {
        runForOutput($$"""
            foo = ["beef", "pork", "cheese"]
            for (x in foo) {
                bar = 0
                baz = 100
                for (i=0; i < 10; i++) {
                    bar += 2
                    while (baz > 20) baz--
                }
                if (foo == "pork") continue
                notifyConn("$bar $x $baz")
            }
        """, """
            20 beef 20
            20 cheese 20
        """)
    }

}
