package com.dlfsystems

import kotlin.test.Test

class TaskTest: YeggTest() {

    @Test
    fun `Suspend`() = yeggTest {
        runForOutput($$"""
            foo = 20
            startTime = $sys.time
            suspend(2)
            elapsedTime = $sys.time - startTime
            notifyConn("$foo in $elapsedTime seconds")
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
                notifyConn("$foo in $elapsed seconds")
            }
            fork (2) {
                elapsed = $sys.time - startTime
                foo *= 3
                notifyConn("$foo in $elapsed seconds")
                fork (1) {
                    elapsed = $sys.time - startTime
                    foo = "ACK"
                    notifyConn("$foo in $elapsed seconds")
                }
            }
            notifyConn("start $foo")
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
            task1 = fork 10 { notifyConn("MERDE") }
            task2 = fork 15 { notifyConn("SCHEIB") }
            task1.cancel()
            notifyConn("start")
            task2.resume()
            suspend(0)
            notifyConn("end")
        """, """
            start
            SCHEIB
            end
        """)
    }

}
