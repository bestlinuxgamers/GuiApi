package net.bestlinuxgamers.guiApi.component

/**
 * Klasse speichert die Reservierung von Slots im zweidimensionalen Raum.
 * @param reservedSlotsArr2D Reservierte Slots (true = reserviert)
 */
class ReservedSlots(private val reservedSlotsArr2D: Array<Array<Boolean>>) {

    /**
     * Für rechteckige Komponenten
     * @see [generateReservedArr2D]
     */
    constructor(height: Int, width: Int) : this(generateReservedArr2D(height, width))

    /**
     * @see [translateArr1DToArr2D]
     */
    @Suppress("UNUSED")
    constructor(width: Int, reservedSlots: Array<Boolean>) : this(translateArr1DToArr2DSquare(width, reservedSlots))

    val size: Int by lazy { calculateSize() }

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
    @Suppress("MemberVisibilityCanBePrivate")
    fun getSizeOfLine(line: Int): Int = reservedSlotsArr2D[line].filter { true }.size

    /**
     * @param index 0 Index
     * @return [Position2D] des [index]ten verfügbaren Elements. X = row, Y = line
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
     * Gibt den eindimensionalen Index im Speicher von einer zweidimensionalen Position zurück.
     * @param position Position (y = Zeile, x = Spalte)
     * @return Index der [position]
     * @throws ArrayIndexOutOfBoundsException Wenn die [position] im Speicher nicht existiert.
     * (Zeile y wurde nicht gesetzt oder Zeile y hat weniger als x Spalten)
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

    //getter

    /**
     * @return Klon vom Speicher der reservierten Slots
     */
    fun getArr2D() = reservedSlotsArr2D.clone()

    companion object {
        /**
         * Generiert den [ReservedSlots] Speicher für rechteckige Strukturen
         * @param height Höhe
         * @param width Breite
         * @return [ReservedSlots] Speicher
         */
        fun generateReservedArr2D(height: Int, width: Int): Array<Array<Boolean>> =
            Array(height) { generateReservedRow(width) }

        /**
         * Übersetzt einen eindimensionalen Speicher in den zweidimensionalen [ReservedSlots] Speicher
         * @param widths Breiten der Zeilen des zweidimensionalen Speichers
         * @param reservedSlots eindimensionaler Speicher
         * @return zweidimensionaler Speicher
         */
        fun translateArr1DToArr2D(widths: Array<Int>, reservedSlots: Array<Boolean>): Array<Array<Boolean>> =
            Array(widths.size) { index ->
                val startIndex = widths.copyOfRange(0, index).sum()
                reservedSlots.copyOfRange(startIndex, startIndex + widths[index])
            }

        /**
         * [translateArr1DToArr2D] mit rechteckiger Struktur
         * @param width Breite des zweidimensionalen Speichers
         * @param reservedSlots eindimensionaler Speicher
         * @return zweidimensionaler Speicher mit rechteckiger Struktur
         */
        fun translateArr1DToArr2DSquare(width: Int, reservedSlots: Array<Boolean>) =
            translateArr1DToArr2D(Array(reservedSlots.size / width) { width }, reservedSlots)

        /**
         * Generiert eine Zeile eines [ReservedSlots] Speichers
         * @param width Breite der Zeile
         * @param reserved Ist die Zeile reserviert?
         * @return Array<Boolean> mit [width] mal [reserved] gefüllt
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun generateReservedRow(width: Int, reserved: Boolean = true): Array<Boolean> =
            Array(width) { reserved }
    }

    /**
     * Repräsentiert eine Position im zweidimensionalen Raum.
     * y = vertikal
     * x = horizontal
     */
    data class Position2D(val x: Int, val y: Int)
}
