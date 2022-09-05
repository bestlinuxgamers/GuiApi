package net.bestlinuxgamers.guiApi.extensions

/**
 * Gibt die value des [key] zurück.
 * Wenn dieser null ist, wird der Standard-Wert auf den [key] gespeichert und zurückgegeben.
 * @return value des [key]
 * @see MutableMap.withDefault
 */
fun <K, V> MutableMap<K, V>.getValueSaved(key: K): V = get(key) ?: getValue(key).also { set(key, it) }
