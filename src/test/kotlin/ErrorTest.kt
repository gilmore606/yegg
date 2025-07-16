package com.dlfsystems.yegg

import com.dlfsystems.yegg.util.YeggTest
import org.junit.Test

class ErrorTest: YeggTest() {

    @Test
    fun `Catch single error`() = yeggTest {
        runForOutput($$"""
            try {
                notifyConn("start")
                foo = [3,5]
                notifyConn(foo[666])
                notifyConn("got away with it!")
            } catch E_RANGE, E_INVARG { e ->
                notifyConn("WHOOPS! $e")
            }
            notifyConn("end")
        ""","""
            start
            WHOOPS! E_RANGE
            end
        """)
    }

    @Test
    fun `Do not catch uncaught error`() = yeggTest {
        runForOutput($$"""
            try {
                notifyConn("start")
                foo = [3,5]
                notifyConn(foo[666])
                notifyConn("got away with it!")
            } catch E_TYPE { e ->
                notifyConn("WHOOPS! $e")
            }
            notifyConn("end")
        ""","""
            start
            E_RANGE: list index 666 out of bounds  (l3 c22)
        """)
    }

    @Test
    fun `Catch with shorter syntax`() = yeggTest {
        runForOutput($$"""
            try notifyConn([1,2,3][0]) catch notifyConn("WHOOPS! $it")
            try notifyConn([1,2,3]["foo"]) catch notifyConn("WHOOPS! $it")
            notifyConn("done")
        ""","""
            1
            WHOOPS! E_TYPE
            done
        """)
    }
}
