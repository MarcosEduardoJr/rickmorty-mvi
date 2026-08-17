pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // mavenLocal primeiro: permite iterar no android-core sem publicar tag.
        mavenLocal()
        google()
        mavenCentral()
        // JitPack constroi o android-core a partir de uma tag publica do GitHub.
        maven("https://jitpack.io")
    }
}

rootProject.name = "rickmorty-mvi"

include(":app")
include(":domain")
include(":data")
include(":feature:characters")
