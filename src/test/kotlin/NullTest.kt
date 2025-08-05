package com.dlfsystems.yegg

import com.dlfsystems.yegg.util.YeggTest
import kotlin.test.Test

class NullTest: YeggTest() {

    @Test
    fun `No void expressions`() = yeggTest {
        runForOutput($$"""
            foo = cnotify("returns void")
            cnotify("OH NO!  $foo")
        """, """
            returns void
            E_VOID: void in expression
        """)
    }

    @Test
    fun `Null type args and coalesce`() = yeggTest {
        verb("sys", "weightOf", $$"""
            [foo: STRING?] = args
            return (foo ?: "default").length
        """)
        runForOutput($$"""
            for (x in ["egg", null, "beer", 5]) {
                w = $sys.weightOf(x)
                cnotify("$x weighs $w")
            }
        """, """
            egg weighs 3
            null weighs 7
            beer weighs 4
            E_TYPE: INT is not STRING
        """)
    }
}
