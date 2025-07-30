package com.dlfsystems.yegg

import com.dlfsystems.yegg.util.YeggTest
import kotlin.test.Test

class VerbTest: YeggTest() {

    @Test
    fun `Verb inheritance on traits`() = yeggTest {
        run($$"""
            createTrait("animal")
            createTrait("dog")
            addParent($dog, $animal)
        """)

        verb("animal", "getNoise", "return \"generic noise\"")
        verb("dog", "getNoise", "return \"bark\"")

        runForOutput($$"""
            o = create($dog)
            cnotify("Dog says: ${o.getNoise()}!")
            a = create($animal)
            cnotify("Animal says: ${a.getNoise()}!")
        """, """
            Dog says: bark!
            Animal says: generic noise!
        """)
    }

    @Test
    fun `Verbs calling other verbs`() = yeggTest {
        verb("sys", "test", $$"""
            [input] = args
            cnotify("test for $input")
            return 5 + $sys.test2(input)
        """)
        verb("sys", "test2", $$"""
            [input] = args
            cnotify("test2 for $input")
            return 3 + $sys.test3(input)
        """)
        verb("sys", "test3", $$"""
            [input] = args
            cnotify("test3 for $input")
            return 1 + input
        """)

        runForOutput($$"""
            cnotify("For 1 : ${$sys.test(1)}")
            cnotify("For 10: ${$sys.test(10)}")
        """, """
            test for 1
            test2 for 1
            test3 for 1
            For 1 : 10
            test for 10
            test2 for 10
            test3 for 10
            For 10: 19
        """)
    }

    @Test
    fun `Resolve to parent verb`() = yeggTest {
        run($$"""
            createTrait("animal")
            createTrait("dog")
            addParent($dog, $animal)
        """)

        verb("animal", "madeOf", "return \"meat\"")

        runForOutput($$"""
            o = create($dog)
            cnotify("Dog is made of: ${o.madeOf()}!")
        """, """
            Dog is made of: meat!
        """)
    }

    @Test
    fun `Pass to parent verb`() = yeggTest {
        run($$"""
            createTrait("animal")
            createTrait("dog")
            addParent($dog, $animal)
        """)

        verb("animal", "getWeight", "return 5")
        verb("dog", "getWeight", "return pass() + 2")
        verb("animal", "makeNoise", $$"""
            [vol] = args
            cnotify("NOISE volume $vol")
        """)
        verb("dog", "makeNoise", $$"""
            [vol] = args
            cnotify("BARK volume $vol")
            return pass(vol)
        """)

        runForOutput($$"""
            o = create($dog)
            cnotify("Dog weighs: ${o.getWeight()}!")
            a = create($animal)
            cnotify("Animal weighs: ${a.getWeight()}!")
            o.makeNoise(10)
        """, """
            Dog weighs: 7!
            Animal weighs: 5!
            BARK volume 10
            NOISE volume 10
        """)
    }

}
