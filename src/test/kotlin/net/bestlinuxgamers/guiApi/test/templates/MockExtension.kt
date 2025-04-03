package net.bestlinuxgamers.guiApi.test.templates

abstract class MockExtension<T> {

    open val dependencies: Set<MockExtension<T>> = setOf()

    abstract fun applyMock(mockClass: T)

}
