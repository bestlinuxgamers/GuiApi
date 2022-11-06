package net.bestlinuxgamers.guiApi.component

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@RequiresOptIn(
    message = "This api should only be used in the render pipeline",
    level = RequiresOptIn.Level.ERROR
)
annotation class RenderOnly


@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@RequiresOptIn(
    message = "This api should only be used a call dispatcher pipeline",
    level = RequiresOptIn.Level.ERROR
)
annotation class CallDispatcherOnly

