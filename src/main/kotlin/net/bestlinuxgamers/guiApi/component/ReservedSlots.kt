package net.bestlinuxgamers.guiApi.component

class ReservedSlots(private val reservedSlotsArr2D: Array<Array<Boolean>>) {

    constructor(height: Int, width: Int) : this(generateReservedMatrix(height, width))

    constructor(width: Int, reservedSlots: Array<Boolean>) : this(generateReservedMatrix(width, reservedSlots))

    val size: Int by lazy { calculateSize() }

    fun getArr2D() = reservedSlotsArr2D.clone()

    /**
     * @return Anzahl aller verfügbaren Slots
     */
    private fun calculateSize(): Int {
        var size = 0
        reservedSlotsArr2D.forEach { size += it.filter { true }.size }
        return size
    }

    /**
     * @param line Zeile (0 Index)
     * @return die Anzahl an verfügbaren Slots in Zeile [line]
     * @throws ArrayIndexOutOfBoundsException Wenn es [line] nicht gibt
     */
    fun getSizeOfLine(line: Int): Int = reservedSlotsArr2D[line].filter { true }.size

    /**
     * @param index 0 Index
     * @return Position des [index]ten verfügbaren Elements. X = row, Y = line
     * @throws ArrayIndexOutOfBoundsException Wenn [index] zu groß ist
     */
    fun getPosOfIndex(index: Int): Position2D {
        if (index > size) throw ArrayIndexOutOfBoundsException("Can't find index $index, because size is $size")

        var count = 0
        reservedSlotsArr2D.forEachIndexed { line, lineData ->
            lineData.forEachIndexed { row, rowData ->
                if (rowData) {
                    if (count == index) return Position2D(row, line)
                    count++
                }
            }
        }
        throw IllegalStateException("Internal error. Contact the developer!")
    }

    /**
     * @param position
     * @return Index der [position]
     * @throws ArrayIndexOutOfBoundsException Wenn es [position] nicht gibt
     */
    fun getIndexOfPos(position: Position2D): Int {
        if (position.x > getSizeOfLine(position.y)) throw ArrayIndexOutOfBoundsException(
            "Can't find ${position.x} in Line ${position.y} because size is ${getSizeOfLine(position.y)}"
        )

        var count: Int = position.x - 1
        for (i in 0 until position.y) {
            count += getSizeOfLine(i)
        }
        return count
    }

    companion object {
        fun generateReservedMatrix(height: Int, width: Int): Array<Array<Boolean>> =
            Array(height) { generateReservedRow(width) }


        fun generateReservedMatrix(width: Int, reservedSlots: Array<Boolean>): Array<Array<Boolean>> {
            val height = width / reservedSlots.size
            val array = Array(height) { generateReservedRow(width, reserved = false) }

            for (i in 0 until height - 1) {
                array[i] = reservedSlots.copyOfRange(
                    i * width,
                    ((i + 1) * width) - 1
                ) //TODO Was, wenn output kleiner als Größe der Zeile
            }
            return array
        }

        fun generateReservedRow(width: Int, reserved: Boolean = true): Array<Boolean> = Array(width) { reserved }
    }

    data class Position2D(val x: Int, val y: Int)
}
