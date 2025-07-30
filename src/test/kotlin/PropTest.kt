package com.dlfsystems.yegg

import com.dlfsystems.yegg.util.YeggTest
import kotlin.test.Test

class PropTest: YeggTest() {

    @Test
    fun `Props on trait`() = yeggTest {
        runForOutput($$"""
            createTrait("thing")
            addProp($thing, "weight", 4)
            cnotify($thing.weight)
            createTrait("weapon")
            addParent($weapon, $thing)
            cnotify($weapon.weight)
            addProp($thing, "size", 1)
            $weapon.size = 5
            cnotify("weapon ${$weapon.size} thing ${$thing.size}")
            clearProp($weapon, "size")
            cnotify("weapon now ${$weapon.size}")
        """, """
            4
            4
            weapon 5 thing 1
            weapon now 1
        """)
    }

    @Test
    fun `Props on obj`() = yeggTest {
        runForOutput($$"""
            createTrait("thing")
            o = create($thing)
            addProp($thing, "weight", 4)
            cnotify("it weighs ${o.weight}")
            o.weight = 12
            cnotify("it weighs ${o.weight}")
            o.clearProp("weight")
            cnotify("it weighs ${o.weight}")
        """, """
            it weighs 4
            it weighs 12
            it weighs 4
        """)
    }

}
