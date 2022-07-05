package net.bestlinuxgamers.guiApi.component.util

import net.bestlinuxgamers.guiApi.component.GuiComponent

/**
 * Dieser Fehler wird geworfen, wenn sich eine Komponente mit einer anderen überlappt
 */
class ComponentOverlapException : RuntimeException()

/**
 * Dieser Fehler wird geworfen, wenn eine [GuiComponent] bereits verwendet wird
 * @see GuiComponent.hook
 */
class ComponentAlreadyInUseException : RuntimeException()

/**
 * Dieser Fehler wird geworfen, wenn eine Rekursion aus Komponenten entstehen würde
 */
class ComponentRekursionException : IllegalArgumentException()
