plugins {
    alias(libs.plugins.rm.jvm.library)
}

dependencies {
    api(libs.core.common)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
