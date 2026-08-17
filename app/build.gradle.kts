plugins {
    alias(libs.plugins.rm.android.application)
    alias(libs.plugins.rm.android.compose)
}

android {
    namespace = "com.mej.rickmorty"

    defaultConfig {
        applicationId = "com.mej.rickmorty"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":feature:characters"))
    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(libs.core.designsystem)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
