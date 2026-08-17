package com.mej.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Catalogo de versoes acessivel de dentro dos convention plugins. */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

object AndroidConfig {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 24
    const val TARGET_SDK = 35
    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17
}

/** Configuracao comum a modulos de aplicacao e de biblioteca Android. */
internal fun Project.configureAndroid(extension: CommonExtension<*, *, *, *, *, *>) {
    extension.apply {
        compileSdk = AndroidConfig.COMPILE_SDK

        defaultConfig {
            minSdk = AndroidConfig.MIN_SDK
        }

        compileOptions {
            sourceCompatibility = AndroidConfig.JAVA_VERSION
            targetCompatibility = AndroidConfig.JAVA_VERSION
        }

        lint {
            warningsAsErrors = false
            abortOnError = true
            checkDependencies = true
        }

        packaging {
            resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(AndroidConfig.JVM_TARGET)
            // Warnings de opt-in viram ruido em modulo com Compose/Material3.
            freeCompilerArgs.addAll("-Xconsistent-data-class-copy-visibility")
        }
    }
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = AndroidConfig.JAVA_VERSION
        targetCompatibility = AndroidConfig.JAVA_VERSION
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(AndroidConfig.JVM_TARGET)
        }
    }
}
