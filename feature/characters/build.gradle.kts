plugins {
    alias(libs.plugins.rm.android.feature)
}

android {
    namespace = "com.mej.rickmorty.feature.characters"
}

dependencies {
    testImplementation(libs.koin.test)
}
