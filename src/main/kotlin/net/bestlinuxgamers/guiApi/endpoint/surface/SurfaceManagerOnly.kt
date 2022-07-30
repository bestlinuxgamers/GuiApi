package net.bestlinuxgamers.guiApi.endpoint.surface

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@RequiresOptIn(
    message = "This api should only be used by classes managing this surface.",
    level = RequiresOptIn.Level.ERROR
)
annotation class SurfaceManagerOnly
