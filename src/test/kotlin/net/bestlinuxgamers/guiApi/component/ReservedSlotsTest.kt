package net.bestlinuxgamers.guiApi.component

import net.bestlinuxgamers.guiApi.component.ReservedSlots.Companion.translateArr1DToArr2D
import net.bestlinuxgamers.guiApi.component.ReservedSlots.Companion.translateArr1DToArr2DSquare
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ReservedSlotsTest {

    @Test
    fun testTranslateArr1DToArr2D() {
        val widths: Array<Int> = arrayOf(2, 1, 3, 0, 5, 4)
        val reserved: Array<Boolean> =
            arrayOf(true, false, true, true, false, true, false, false, true, false, true, true, true, true, true)

        val target: Array<Array<Boolean>> = arrayOf(
            arrayOf(true, false),
            arrayOf(true),
            arrayOf(true, false, true),
            emptyArray(),
            arrayOf(false, false, true, false, true),
            arrayOf(true, true, true, true)
        )

        val result: Array<Array<Boolean>> = translateArr1DToArr2D(widths, reserved)

        Assertions.assertEquals(target.map { it.contentToString() }, result.map { it.contentToString() })
    }

    @Test
    fun testTranslateArr1DToArr2DSquare() {
        val width = 3
        val reserved = arrayOf(true, false, true, false, false, false, false, true, false)

        val target = arrayOf(arrayOf(true, false, true), arrayOf(false, false, false), arrayOf(false, true, false))

        val result = translateArr1DToArr2DSquare(width, reserved)

        Assertions.assertEquals(target.map { it.contentToString() }, result.map { it.contentToString()})
    }
}
