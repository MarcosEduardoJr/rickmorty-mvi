plugins {
    `kotlin-dsl`
}

group = "com.mej.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
}

/**
 * Cada plugin publicado aqui vira um id aplicavel nos modulos.
 * Centralizar a configuracao evita `build.gradle.kts` duplicado por modulo,
 * que e a principal fonte de divergencia em projeto multi-modulo.
 */
gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "rm.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "rm.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "rm.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "rm.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "rm.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
