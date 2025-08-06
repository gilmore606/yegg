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

    @Test
    fun `Null-safe verb and prop refs`() = yeggTest {
        run($$"""
            createTrait("animal")
            addProp($animal, "weight", 10)
        """)
        verb("animal", "getSize", "return this.weight * 3")
        runForOutput($$"""
            a1 = create($animal)
            a1.weight = 4
            a2 = create($animal)
            a2.weight = 7
            a3 = create($animal)
            a3.weight = 10
            for (x in [a1, a2, null, a3]) {
                w = x?.weight ?: 666
                size = x?.getSize() ?: 0
                cnotify("$w and $size")
                x?.weight = 12
                if ((x?.weight ?: 12) != 12) cnotify("OH NO!")
            }
        ""","""
            4 and 12
            7 and 21
            666 and 0
            10 and 30
        """)
    }
}
