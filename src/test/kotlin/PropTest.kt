package com.dlfsystems

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class PropTest: YeggTest() {

    @Test
    fun `Props on trait`() = runBlocking {
        runForOutput($$"""
            createTrait("thing")
            addProp($thing, "weight", 4)
            notifyConn($thing.weight)
            createTrait("weapon")
            addParent($weapon, $thing)
            notifyConn($weapon.weight)
            addProp($thing, "size", 1)
            $weapon.size = 5
            notifyConn("weapon ${$weapon.size} thing ${$thing.size}")
            clearProp($weapon, "size")
            notifyConn("weapon now ${$weapon.size}")
        """, """
            4
            4
            weapon 5 thing 1
            weapon now 1
        """)
    }

}
