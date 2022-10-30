package net.bestlinuxgamers.guiApi.component

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@RequiresOptIn(
    message = "This api should only be used in the render pipeline",
    level = RequiresOptIn.Level.ERROR
)
annotation class RenderOnly
