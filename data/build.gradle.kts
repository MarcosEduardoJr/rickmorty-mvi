plugins {
    alias(libs.plugins.rm.android.library)
    alias(libs.plugins.apollo)
}

android {
    namespace = "com.mej.rickmorty.data"
}

apollo {
    service("rickandmorty") {
        packageName.set("com.mej.rickmorty.graphql")
        // Gera o schema a partir do arquivo versionado, sem depender de rede no build.
        schemaFiles.from(file("src/main/graphql/com/mej/rickmorty/graphql/schema.graphqls"))
    }
}

dependencies {
    api(project(":domain"))
    api(libs.core.network)
    implementation(libs.core.common)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
