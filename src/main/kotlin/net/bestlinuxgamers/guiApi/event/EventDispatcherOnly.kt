package net.bestlinuxgamers.guiApi.event

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@RequiresOptIn(
    message = "This api should only be used in the event dispatcher pipeline",
    level = RequiresOptIn.Level.ERROR
)
annotation class EventDispatcherOnly
