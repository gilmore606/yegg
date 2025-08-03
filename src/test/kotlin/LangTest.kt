package com.dlfsystems.yegg

import com.dlfsystems.yegg.util.YeggTest
import kotlin.test.Test

class LangTest: YeggTest() {

    @Test
    fun `Math`() = yeggTest {
        runForOutput($$"""
            cnotify(14 * 10 + 71 / 3 ^ 3)
            cnotify(500 - 100 / 2 * 5)
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
            cnotify("foo is $foo")
            bar = foo
            foo = 13
            cnotify("bar is $bar")
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
            cnotify("$bar = ${bar.length}")
            newdick = bar.split(",")[1].replace("eggs", "cheese")
            cnotify(newdick)
        """, """
            eggstom,eggsdick,eggs harry = 27
            cheesedick
        """)
    }

    @Test
    fun `Lists`() = yeggTest {
        runForOutput($$"""
            foo = ["beef", "pork", "cheese"]
            cnotify(foo[1..2])
            bar = ["eggs", "milk"]
            cnotify(foo + bar)
            baz = [foo[1], bar[1]]
            cnotify(baz)
            baz.setAddAll(["milk", "pee"])
            cnotify(baz)
            baz.push("poo")
            if (baz.first == "poo") cnotify("poohead")
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
            cnotify("${fooMap.keys}")
            for (i=0;i<fooMap.keys.size;i++) {
                j = 0
                poo = 1
                animal = fooMap.keys[i]
                while (j < fooMap[fooMap.keys[i]]) {
                    poo++
                    j++
                }
                cnotify("$animal $poo")
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
                if (i == 1) cnotify("one")
                else if (i == 3) { 
                    cnotify("three") 
                } else cnotify("two")
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
                cnotify("$bar $x $baz")
            }
        """, """
            20 beef 20
            20 cheese 20
        """)
    }

    @Test
    fun `For loop over empty list is skipped`() = yeggTest {
        runForOutput($$"""
            foo = []
            for (x in foo) cnotify("ACK")
            foo = [1]
            for (x in foo) cnotify("BAR")
            cnotify("done")
        """, """
            BAR
            done
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
                    foo.contains("two") -> cnotify("$foo has two")
                }
            }
            cnotify(foo)
        """, """
            onetwo has two
            onetwothree has two
            onetwothree??? has two
            onetwothree???
        """)
    }

    @Test
    fun `Logical OR and AND short circuits right side`() = yeggTest {
        runForOutput($$"""
            true || cnotify("FAILED 1")
            true && cnotify("OK 1.")
            false && cnotify("FAILED 2")
            false || cnotify("OK 2.")
            foo = (1 == 1) && (false || true)
            cnotify("Foo is $foo.")
            bar = (1 == 2) && true
            cnotify("Bar is $bar.")
        """, """
            OK 1.
            OK 2.
            Foo is true.
            Bar is false.
        """)
    }

    @Test
    fun `Mixed list sorted by first value type`() = yeggTest {
        runForOutput($$"""
            foo = [36, 9, "hello", 88, 5.6]
            cnotify(foo.sorted)
        """, """
            "hello", 5.6, 9, 36, 88
        """)
    }

    @Test
    fun `List filter`() = yeggTest {
        runForOutput($$"""
            foo = [1,5,7,12,26,31,74].filter({ it % 2 == 0 })
            cnotify(foo)
        """, """
            12, 26, 74
        """)
    }

    @Test
    fun `List map`() = yeggTest {
        verb("sys", "resultOf", $$"""
            [input] = args
            return input * 10
        """)
        runForOutput($$"""
            foo = [1,3,5].map({ "got ${$sys.resultOf(it)}" })
            for (x in foo) cnotify("$x")
        """, """
            got 10
            got 30
            got 50
        """)
    }

    @Test
    fun `List sortedBy`() = yeggTest {
        runForOutput($$"""
            foo = ["beer", "egg", "cheese", "me"].sortedBy({ it.length })
            for (x in foo) cnotify("$x")
        """, """
            me
            egg
            beer
            cheese
        """)
    }

    @Test
    fun `List destructure with types`() = yeggTest {
        verb("sys", "looseFun", $$"""
            [foo, bar] = args
            cnotify("loose $foo and $bar")
        """)
        verb("sys", "tightFun", $$"""
            [foo: FLOAT, bar: STRING] = args
            cnotify("tight $foo and $bar")
        """)

        runForOutput($$"""
            $sys.looseFun(12.6, 74)
            $sys.tightFun(12.6, 74)
        """, """
            loose 12.6 and 74
            E_INVARG: INT is not STRING
        """)
    }

    @Test
    fun `String replaceMap`() = yeggTest {
        runForOutput($$"""
            template = "%N smacks %t in the face!"
            final = template.replaceMap(["%t": "Jake Wharton", "%d": "Gilmore", "%N": "Spunky"])
            cnotify(final)
        ""","""
            Spunky smacks Jake Wharton in the face!
        """)
    }

    @Test
    fun `x is TYPE`() = yeggTest {
        verb("sys", "isString", $$"""
            [foo] = args
            return foo is STRING
        """)
        runForOutput($$"""
            for (x in ["egg", "beer", 42, $sys]) {
                if ($sys.isString(x)) cnotify("$x is a string")
            }
        ""","""
            egg is a string
            beer is a string
        """)
    }

    @Test
    fun `x is trait`() = yeggTest {
        run($$"""
            createTrait("animal")
            createTrait("dog")
            addParent($dog, $animal)
        """)
        runForOutput($$"""
            dog = create($dog)
            animal = create($animal)
            bob = create($player)
            if (bob is $animal) cnotify("Bob's an animal!")
            if (dog is $animal) cnotify("Dogs are animals.")
        """, """
            Dogs are animals.
        """)
    }
}
