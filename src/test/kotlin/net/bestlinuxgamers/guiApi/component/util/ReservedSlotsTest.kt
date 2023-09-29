package net.bestlinuxgamers.guiApi.component.util

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots.Companion.generateReservedArr2D
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots.Companion.translateArr1DToArr2D
import net.bestlinuxgamers.guiApi.component.util.ReservedSlots.Companion.translateArr1DToArr2DSquare
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ReservedSlotsTest {

    //companion
    @Test
    fun testGenerateReservedArr2D() {
        val target =
            arrayOf(arrayOf(true, true, true, true), arrayOf(true, true, true, true), arrayOf(true, true, true, true))
        val result = generateReservedArr2D(3, 4)

        Assertions.assertArrayEquals(target, result)
    }

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

        Assertions.assertArrayEquals(target, result)
    }

    @Test
    fun testTranslateArr1DToArr2DSquare() {
        val width = 3
        val reserved = arrayOf(true, false, true, false, false, false, false, true, false)

        val target = arrayOf(arrayOf(true, false, true), arrayOf(false, false, false), arrayOf(false, true, false))

        val result = translateArr1DToArr2DSquare(width, reserved)

        Assertions.assertArrayEquals(target, result)
    }

    //ReservedSlots

    @Test
    fun testTotalCount() {
        val target = ReservedSlots(3, 7)
        val reserved = ReservedSlots(
            arrayOf(
                arrayOf(false, true),
                arrayOf(false, true),
                arrayOf(true, false, true),
                emptyArray(),
                arrayOf(false, false, true, false, true),
                arrayOf(true, true, true, true)
            )
        )

        Assertions.assertEquals(21, target.totalReserved)
        Assertions.assertEquals(10, reserved.totalReserved)
    }

    @Test
    fun testTrim() {
        val reserved1: Array<Array<Boolean>> = arrayOf(
            arrayOf(false),
            arrayOf(true, false),
            arrayOf(true),
            arrayOf(true, false, true),
            emptyArray(),
            arrayOf(false, false, true, false, true, false, false, false, false),
            arrayOf(false),
            arrayOf(true, true, true, true),
            arrayOf(false, false, false, false)
        )
        val reservedSlots1 = ReservedSlots(reserved1)

        val target1 = arrayOf(
            arrayOf(true),
            arrayOf(true),
            arrayOf(true, false, true),
            emptyArray(),
            arrayOf(false, false, true, false, true),
            emptyArray(),
            arrayOf(true, true, true, true),
        )


        val reserved2: Array<Array<Boolean>> =
            arrayOf(arrayOf(false, false), arrayOf(false), arrayOf(false, true), arrayOf(true))
        val reservedSlots2 = ReservedSlots(reserved2)

        val target2: Array<Array<Boolean>> = arrayOf(arrayOf(false, true), arrayOf(true))


        val target3err = arrayOf(
            arrayOf(false),
            arrayOf(false),
            arrayOf(false, false, false),
            emptyArray(),
            arrayOf(false, false, false, false, false),
            emptyArray(),
            arrayOf(false, false, false, false),
        )


        Assertions.assertArrayEquals(target1, reservedSlots1.getArr2D())
        Assertions.assertArrayEquals(target2, reservedSlots2.getArr2D())
        Assertions.assertThrows(NoReservedSlotsException::class.java) { ReservedSlots(target3err) }
        Assertions.assertThrows(NoReservedSlotsException::class.java) { ReservedSlots(0, 4) }
        Assertions.assertThrows(NoReservedSlotsException::class.java) { ReservedSlots(2, 0) }
    }

    @Test
    fun testCalculateReservedSize() {
        val reserved: Array<Array<Boolean>> = arrayOf(
            arrayOf(true, false),
            arrayOf(true),
            arrayOf(true, false, true),
            emptyArray(),
            arrayOf(false, false, true, false, true),
            arrayOf(true, true, true, true)
        )
        val reservedSlots = ReservedSlots(reserved)

        val targetSize = 10

        Assertions.assertEquals(targetSize, reservedSlots.totalReserved)
    }

    @Test
    fun testGetReservedOfLine() {
        val reserved: Array<Array<Boolean>> = arrayOf(
            arrayOf(true, false),
            arrayOf(true),
            arrayOf(true, false, true),
            emptyArray(),
            arrayOf(false, false, true, false, true),
            arrayOf(true, true, true, true)
        )
        val reservedSlots = ReservedSlots(reserved)

        val line0 = 1
        val line1 = 1
        val line2 = 2
        val line3 = 0
        val line4 = 2
        val line5 = 4

        Assertions.assertEquals(line0, reservedSlots.getReservedOfLine(0))
        Assertions.assertEquals(line1, reservedSlots.getReservedOfLine(1))
        Assertions.assertEquals(line2, reservedSlots.getReservedOfLine(2))
        Assertions.assertEquals(line3, reservedSlots.getReservedOfLine(3))
        Assertions.assertEquals(line4, reservedSlots.getReservedOfLine(4))
        Assertions.assertEquals(line5, reservedSlots.getReservedOfLine(5))
        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) { reservedSlots.getReservedOfLine(6) }
        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) { reservedSlots.getReservedOfLine(-1) }
    }

    @Test
    fun testGetPosOfIndex() {
        val reserved: Array<Array<Boolean>> = arrayOf(
            arrayOf(true),
            arrayOf(true),
            arrayOf(true, false, true),
            arrayOf(false),
            arrayOf(false, false, true, false, true),
            arrayOf(true, true, true, true)
        )
        val reservedSlots = ReservedSlots(reserved)

        val idx1 = 0
        val pos1 = Position2D(1, 1)

        val idx2 = 3
        val pos2 = Position2D(3, 3)

        val idx3 = 7
        val pos3 = Position2D(2, 6)


        val reserved2: Array<Array<Boolean>> =
            arrayOf(arrayOf(false, false), arrayOf(false), arrayOf(false, true), arrayOf(true))
        val reservedSlots2 = ReservedSlots(reserved2)

        val pos1r2 = Position2D(2, 1)

        Assertions.assertEquals(pos1, reservedSlots.getPosOfReservedIndex(idx1))
        Assertions.assertEquals(pos2, reservedSlots.getPosOfReservedIndex(idx2))
        Assertions.assertEquals(pos3, reservedSlots.getPosOfReservedIndex(idx3))
        Assertions.assertEquals(pos1r2, reservedSlots2.getPosOfReservedIndex(idx1))
        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) { reservedSlots.getPosOfReservedIndex(-1) }
        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) { reservedSlots.getPosOfReservedIndex(100) }
    }

    @Test
    fun testGetReservedIndexOfPos() {
        val reserved: Array<Array<Boolean>> = arrayOf(
            arrayOf(true, false),
            arrayOf(true),
            arrayOf(true, false, true),
            emptyArray(),
            arrayOf(false, false, true, false, true),
            arrayOf(true, true, true, true)
        )
        val reservedSlots = ReservedSlots(reserved)

        val pos1 = Position2D(1, 1)
        val targetPos1 = 0

        val pos2 = Position2D(1, 3)
        val targetPos2 = 2

        val pos3 = Position2D(4, 6)
        val targetPos3 = 9

        val pos4 = Position2D(5, 5)
        val targetPos4 = 5

        val pos5 = Position2D(3, 5)
        val targetPos5 = 4

        val errPos1 = Position2D(0, 3)
        val errPos2 = Position2D(1, 0)
        val errPos3 = Position2D(1, 4)
        val errPos4 = Position2D(6, 5)
        val errPos5 = Position2D(2, 3)

        Assertions.assertEquals(targetPos1, reservedSlots.getReservedIndexOfPos(pos1))
        Assertions.assertEquals(targetPos2, reservedSlots.getReservedIndexOfPos(pos2))
        Assertions.assertEquals(targetPos3, reservedSlots.getReservedIndexOfPos(pos3))
        Assertions.assertEquals(targetPos4, reservedSlots.getReservedIndexOfPos(pos4))
        Assertions.assertEquals(targetPos5, reservedSlots.getReservedIndexOfPos(pos5))
        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            reservedSlots.getReservedIndexOfPos(
                errPos1
            )
        }
        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            reservedSlots.getReservedIndexOfPos(
                errPos2
            )
        }
        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            reservedSlots.getReservedIndexOfPos(
                errPos3
            )
        }
        Assertions.assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            reservedSlots.getReservedIndexOfPos(
                errPos4
            )
        }
        Assertions.assertThrows(SlotNotReservedException::class.java) { reservedSlots.getReservedIndexOfPos(errPos5) }

    }
}
