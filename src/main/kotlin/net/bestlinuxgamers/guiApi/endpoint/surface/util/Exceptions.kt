package net.bestlinuxgamers.guiApi.endpoint.surface.util

/**
 * Dieser Fehler wird geworfen, wenn ein Surface bereits benutzt wird
 */
class SurfaceAlreadyInUseException : IllegalStateException()

/**
 * Dieser Fehler wird geworfen, wenn ein Display bereits geöffnet ist
 */
class DisplayAlreadyOpenedException : IllegalStateException()

/**
 * Dieser Fehler wird geworfen, wenn eine Aktion ausgeführt wird,
 * welche einen registrierten Endpoint benötigt, welcher nicht vorhanden ist.
 */
class NoEndpointRegisteredException : IllegalStateException()
