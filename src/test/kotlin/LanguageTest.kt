package com.dlfsystems

import com.dlfsystems.server.Yegg
import com.dlfsystems.value.VInt
import com.dlfsystems.value.Value
import com.dlfsystems.vm.Context
import com.dlfsystems.world.trait.Verb
import kotlin.test.Test
import kotlin.test.assertEquals

class LanguageTest {

    @Test
    fun `variables`() {
        val source = """
            foo = 1
            foo = 2
            return foo
        """
        val result = runYegg(source)
        assertEquals(result, VInt(1))
    }

    @Test
    fun `math`() {
        val source = """
            [a1, a2] = args
            return a1 * a2
        """
        assertEquals(
            runYegg(source, listOf(VInt(4), VInt(9))),
            VInt(36)
        )
    }


    fun runYegg(source: String, args: List<Value> = listOf()): Value =
        Verb("test", Yegg.world.sys.id).apply { program(source) }
            .call(Context(), Yegg.vNullObj, args)

}
