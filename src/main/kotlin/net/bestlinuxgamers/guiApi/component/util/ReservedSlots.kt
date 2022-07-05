package net.bestlinuxgamers.guiApi.component.util

/**
 * Klasse speichert die Reservierung von Slots im zweidimensionalen Raum.
 * @param reservedSlotsArr2D Reservierte Slots (true = reserviert)
 * @throws NoReservedSlotsException Falls der angegebene Speicher keine reservierten Slots beinhaltet
 */
class ReservedSlots(reservedSlotsArr2D: Array<Array<Boolean>>) {

    private val reservedSlotsArr2D: Array<Array<Boolean>>

    init {
        //ensure every row ends with true
        reservedSlotsArr2D.forEachIndexed { idx, it ->
            if (it.isNotEmpty() && !it.last()) {
                var lastTrueIdx = -1
                it.forEachIndexed { idx2, it2 -> if (it2) lastTrueIdx = idx2 }
                reservedSlotsArr2D[idx] = if (lastTrueIdx > -1) it.copyOfRange(0, lastTrueIdx + 1) else emptyArray()
            }
        }

        //ensure reservedSlots doesn't start and end with an empty array
        var startArr = 0
        var endArr = reservedSlotsArr2D.size
        reservedSlotsArr2D.forEachIndexed { idx, it ->
            if (it.isNotEmpty()) {
                endArr = idx + 1
            } else {
                if (startArr == idx) startArr++
            }
        }
        if (startArr == endArr) throw NoReservedSlotsException()
        this.reservedSlotsArr2D = if (startArr != 0 || endArr != reservedSlotsArr2D.size - 1) {
            reservedSlotsArr2D.copyOfRange(startArr, endArr)
        } else {
            reservedSlotsArr2D
        }
    }

    /**
     * Für rechteckige Komponenten
     * @see [generateReservedArr2D]
     */
    constructor(height: Int, width: Int) : this(generateReservedArr2D(height, width))

    /**
     * @see [translateArr1DToArr2D]
     */
    @Suppress("unused")
    constructor(width: Int, reservedSlots: Array<Boolean>) : this(translateArr1DToArr2DSquare(width, reservedSlots))

    //reservedSlots
    val totalReserved: Int by lazy { calculateReservedSize() }

    /**
     * @return Anzahl aller **verfügbaren** Slots
     */
    private fun calculateReservedSize(): Int = reservedSlotsArr2D.sumOf { it.filter { it2 -> it2 }.size }

    /**
     * @param line Zeile (0 Index)
     * @return die Anzahl an **verfügbaren** Slots in Zeile [line]
     * @throws ArrayIndexOutOfBoundsException Wenn es [line] nicht gibt
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getReservedOfLine(line: Int): Int = reservedSlotsArr2D[line].filter { it }.size

    //allSlots

    private val arrSize by lazy { calculateArrSize() }

    /**
     * @return Anzahl aller Slots (verfügbar und nicht verfügbar)
     */
    private fun calculateArrSize(): Int = reservedSlotsArr2D.sumOf { it.size }

    /**
     * @param line Zeile (0 Index)
     * @return die Anzahl aller Slots (verfügbar und nicht verfügbar) in der Zeile [line]
     */
    private fun getArrSizeOfLine(line: Int): Int = reservedSlotsArr2D[line].size

    /**
     * @param index 0 Index eines **verfügbaren** Slots
     * @return [Position2D] des [index]ten **verfügbaren** Elements. X = row, Y = line
     * @throws ArrayIndexOutOfBoundsException Wenn [index] nicht existiert
     */
    fun getPosOfIndex(index: Int): Position2D {
        if (index >= arrSize) throw ArrayIndexOutOfBoundsException("Can't find index $index, because size is $arrSize")
        if (index < 0) throw ArrayIndexOutOfBoundsException("Index can't be negative'")

        var count = 0
        reservedSlotsArr2D.forEachIndexed { line, lineData ->
            lineData.forEachIndexed { row, rowData ->
                if (rowData) {
                    if (count == index) return Position2D(row + 1, line + 1)
                    count++
                }
            }
        }
        throw ArrayIndexOutOfBoundsException()
    }

    /**
     * Gibt den eindimensionalen Index im Speicher von einer zweidimensionalen Position zurück.
     * @param position Position (y = Zeile, x = Spalte)
     * @return Index der [position]
     * @throws ArrayIndexOutOfBoundsException Wenn die [position] im Speicher nicht existiert.
     * (Zeile y wurde nicht gesetzt oder Zeile y hat weniger als x Spalten)
     */
    fun getIndexOfPos(position: Position2D): Int {
        if (position.x <= 0) throw ArrayIndexOutOfBoundsException("Row can't be negative!")
        val targetLineSize = getArrSizeOfLine(position.y - 1)
        if (position.x > targetLineSize) throw ArrayIndexOutOfBoundsException(
            "Can't find ${position.x} in line ${position.y}, because size is $targetLineSize!"
        )

        var count: Int = position.x - 1
        for (i in 0 until position.y - 1) {
            count += getArrSizeOfLine(i)
        }
        return count
    }

    //getter

    /**
     * @return Klon vom Speicher der reservierten Slots
     */
    fun getArr2D() = reservedSlotsArr2D.clone()

    /**
     * Dieser Fehler wird geworfen, wenn ein angegebener Speicher keine reservierten Slots enthält
     */
    class NoReservedSlotsException : IllegalArgumentException()

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
        fun generateReservedRow(width: Int, reserved: Boolean = true): Array<Boolean> = Array(width) { reserved }
    }

}