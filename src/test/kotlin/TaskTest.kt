package com.dlfsystems.yegg

import com.dlfsystems.yegg.util.YeggTest
import kotlin.test.Test

class TaskTest: YeggTest() {

    @Test
    fun `Output arrives in order`() = yeggTest {
        runForOutput($$"""
            for (i in 0..99) {
                cnotify("$i")
            }
        """, buildList { repeat(100) { add("$it")} }.joinToString("\n"))
    }

    @Test
    fun `Suspend`() = yeggTest {
        runForOutput($$"""
            foo = 20
            startTime = $sys.time
            suspend(2)
            elapsedTime = $sys.time - startTime
            cnotify("$foo in $elapsedTime seconds")
        """, """
            20 in 2 seconds
        """)
    }

    @Test
    fun `Fork`() = yeggTest {
        runForOutput($$"""
            foo = 20
            startTime = $sys.time
            fork (1) {
                elapsed = $sys.time - startTime
                foo *= 2
                cnotify("$foo in $elapsed seconds")
            }
            fork (2) {
                elapsed = $sys.time - startTime
                foo *= 3
                cnotify("$foo in $elapsed seconds")
                fork (1) {
                    elapsed = $sys.time - startTime
                    foo = "ACK"
                    cnotify("$foo in $elapsed seconds")
                }
            }
            cnotify("start $foo")
        """, """
            start 20
            40 in 1 seconds
            60 in 2 seconds
            ACK in 3 seconds
        """)
    }

    @Test
    fun `Cancel and resume`() = yeggTest {
        runForOutput($$"""
            task1 = fork 10 { cnotify("MERDE") }
            task2 = fork 15 { cnotify("SCHEIB") }
            task1.cancel()
            cnotify("start")
            task2.resume()
            suspend(1)
            cnotify("end")
        """, """
            start
            SCHEIB
            end
        """)
    }

    @Test
    fun `Read single line`() = yeggTest {
        verb("sys", "readSingleLine", $$"""
            cnotify("Enter name:")
            name = readLine()
            cnotify("Hi $name!")
        """)

        commandsForOutput($$"""
            ;$sys.readSingleLine()
            Dan
        """, """
            Enter name:
            Hi Dan!
        """)
    }

    @Test
    fun `Read multiple lines`() = yeggTest {
        verb("sys", "readMultiLines", $$"""
            cnotify("Enter some things:")
            things = readLines()
            cnotify("You entered ${things.size} things!")
        """)

        commandsForOutput($$"""
            ;$sys.readMultiLines()
            cheese
            pork
            eggs
            .
        """, """
            Enter some things:
            You entered 3 things!
        """)
    }

}
