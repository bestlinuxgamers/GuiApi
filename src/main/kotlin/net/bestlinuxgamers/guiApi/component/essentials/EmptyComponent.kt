package net.bestlinuxgamers.guiApi.component.essentials

import net.bestlinuxgamers.guiApi.component.util.ReservedSlots

/**
 * Leere Komponente.
 * Slots im Inventar sind leer.
 * @param reservedSlots Oberflächen-Struktur der Komponente
 */
class EmptyComponent(reservedSlots: ReservedSlots) : RenderEndpointComponent(HOLE, reservedSlots) {
    constructor() : this(ReservedSlots(1, 1))
}
