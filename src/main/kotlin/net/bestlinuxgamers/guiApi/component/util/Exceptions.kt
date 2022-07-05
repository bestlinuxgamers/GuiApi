package net.bestlinuxgamers.guiApi.component.util

import net.bestlinuxgamers.guiApi.component.GuiComponent

/**
 * Dieser Fehler wird geworfen, wenn sich eine Komponente mit einer anderen überlappt
 */
class ComponentOverlapException : IllegalArgumentException()

/**
 * Dieser Fehler wird geworfen, wenn eine [GuiComponent] bereits verwendet wird
 * @see GuiComponent.hook
 */
class ComponentAlreadyInUseException : IllegalArgumentException()

/**
 * Dieser Fehler wird geworfen, wenn eine Rekursion aus Komponenten entstehen würde
 */
class ComponentRekursionException : IllegalArgumentException()

/**
 * Dieser Fehler wird geworfen, wenn ein angegebener Speicher keine reservierten Slots enthält
 */
class NoReservedSlotsException : IllegalArgumentException()

/**
 * Dieser Fehler wird geworfen, wenn versucht wird einen nicht reservierten Slot zu benutzen
 */
class SlotNotReservedException : ArrayIndexOutOfBoundsException {
    constructor() : super()
    constructor(message: String) : super(message)
}


//TODO eigene ArrayIndexOutOfBoundsException
