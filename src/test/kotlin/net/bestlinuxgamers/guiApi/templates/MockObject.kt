package net.bestlinuxgamers.guiApi.templates

abstract class MockObject<T> {

    private val extensions: MutableSet<MockExtension<T>> = mutableSetOf()
    private var mockObject: T = createMock()
    private var applied = false

    internal abstract fun createMock(): T

    internal abstract fun applyMock(mockObj: T)

    fun addExtension(extension: MockExtension<T>) {
        val extensionClass = extension::class
        extensions.forEach { if (it::class == extensionClass) return }
        extensions.add(extension)
        extension.dependencies.forEach { addExtension(it) }
        if (applied) extension.applyMock(mockObject)
    }

    fun apply() {
        if (applied) return
        applied = true
        applyMock(mockObject)
        extensions.forEach { it.applyMock(mockObject) }
    }

    fun getMockObject() = mockObject
}
