package com.dlfsystems

import kotlin.test.Test

class LangTest: YeggTest() {

    @Test
    fun `Math`() = yeggTest {
        runForOutput($$"""
            notifyConn(14 * 10 + 71 / 3 ^ 3)
            notifyConn(500 - 100 / 2 * 5)
        """, """
            142
            250
        """)
    }

    @Test
    fun `Variables`() = yeggTest {
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
    fun `Strings`() = yeggTest {
        runForOutput($$"""
            foo = "eggs"
            bag = [foo + "tom", foo + "dick", "$foo harry"]
            bar = bag.join(",")
            notifyConn("$bar = ${bar.length}")
            newdick = bar.split(",")[1].replace("eggs", "cheese")
            notifyConn(newdick)
        """, """
            eggstom,eggsdick,eggs harry = 27
            cheesedick
        """)
    }

    @Test
    fun `Lists`() = yeggTest {
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
    fun `Maps`() = yeggTest {
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
    fun `If-else`() = yeggTest {
        runForOutput($$"""
            for (i in 1..3) {
                if (i == 1) notifyConn("one")
                else if (i == 3) { 
                    notifyConn("three") 
                } else notifyConn("two")
            }
        """, """
            one
            two
            three
        """)
    }

    @Test
    fun `For-while loops`() = yeggTest {
        runForOutput($$"""
            foo = ["beef", "pork", "cheese"]
            for (x in foo) {
                bar = 0
                baz = 100
                for (i=0; i < 99; i++) {
                    if (i >= 10) break
                    bar += 2
                    while (baz > 20) baz--
                }
                if (x == "pork") continue
                notifyConn("$bar $x $baz")
            }
        """, """
            20 beef 20
            20 cheese 20
        """)
    }

    @Test
    fun `When`() = yeggTest {
        runForOutput($$"""
            foo = ""
            for (i in 1..4) {
                foo += when (i) {
                    1 -> "one"
                    2 -> "two"
                    3 -> "three"
                    else -> "???"
                }
                when {
                    foo.contains("two") -> notifyConn("$foo has two")
                }
            }
            notifyConn(foo)
        """, """
            onetwo has two
            onetwothree has two
            onetwothree??? has two
            onetwothree???
        """)
    }

    @Test
    fun `Logical OR short circuits right side`() = yeggTest {
        runForOutput($$"""
            true || notifyConn("Success.")
            false || notifyConn("FAILURE!")
            notifyConn("Done.")
        """, """
            Success.
            Done.
        """)
    }

}
