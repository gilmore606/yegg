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
            WHOOPS! E_RANGE: list index 666 out of bounds
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
            WHOOPS! E_TYPE: cannot index into LIST with STRING
            done
        """)
    }

    @Test
    fun `Catch from deep stack`() = yeggTest {
        verb("sys", "test", $$"""
            [input] = args
            notifyConn("test for $input")
            return 5 + $sys.test2(input)
        """)
        verb("sys", "test2", $$"""
            [input] = args
            notifyConn("test2 for $input")
            return 3 + $sys.VerbNotFound(input)
        """)

        runForOutput($$"""
            try {
                notifyConn("start")
                notifyConn($sys.test(10))
            } catch E_VERBNF {
                notifyConn("WHOOPS!")
            }
            notifyConn("end")
        ""","""
            start
            test for 10
            test2 for 10
            WHOOPS!
            end
        """)
    }

    @Test
    fun `Try expression`() = yeggTest {
        verb("sys", "getWeight", $$"""
            [input] = args
            return 24 / input.length
        """)

        runForOutput($$"""
            for x in ["dog", "duck", 42, ""] {
                weight = `$sys.getWeight(x) ! E_PROPNF -> "???"'
                notifyConn("$x weighs $weight")
            }
        ""","""
            dog weighs 8
            duck weighs 6
            42 weighs ???
            E_DIV: divide by zero  (l1 c24)
        """)
    }
}
