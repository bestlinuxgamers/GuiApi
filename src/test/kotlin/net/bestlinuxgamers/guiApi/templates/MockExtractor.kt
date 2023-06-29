package net.bestlinuxgamers.guiApi.templates

abstract class MockExtractor<T, R>(private val onlyOneSet: Boolean = false) {

    constructor(input: T, onlyOneSet: Boolean = false) : this(onlyOneSet) {
        setup(input)
    }

    private val hooks: MutableSet<(R) -> Unit> = mutableSetOf()
    private var setCount = 0
    private var value: R? = null
    private var lastGet: Int? = null

    abstract fun setup(input: T)

    fun setValue(value: R) {
        if (onlyOneSet && isAlreadySet()) return
        setCount++
        this.value = value
        handleHooks(value)
    }

    private fun handleHooks(value: R) {
        hooks.forEach { it(value) }
    }

    fun addHook(hook: (R) -> Unit) {
        hooks.add(hook)
    }

    /**
     * @throws ValueNotInitializedException
     */
    fun get(): R? {
        if (!isAlreadySet()) throw ValueNotInitializedException()
        lastGet = setCount
        return value
    }

    /**
     * @throws ValueNotChangedException
     * @throws ValueNotInitializedException
     */
    fun getDifferent(): R? {
        if (lastGet == setCount) throw ValueNotChangedException()
        return get()
    }

    fun getSetCount() = setCount

    fun isAlreadySet() = setCount >= 1

    class ValueNotInitializedException : Exception()
    class ValueNotChangedException : Exception()
}
