package net.bestlinuxgamers.guiApi.extensions

fun <T> Array<T>.replaceMut(target: T, replacement: T): Array<T> {
    for (i in this.indices) {
        if (this[i] == target) {
            this[i] = replacement
        }
    }
    return this
}

fun <T> Array<T>.replace(target: T, replacement: T): Array<T> {
    return this.clone().replaceMut(target, replacement)
}
