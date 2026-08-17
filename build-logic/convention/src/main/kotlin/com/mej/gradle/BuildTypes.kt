package com.mej.gradle

import com.android.build.api.dsl.ApplicationBuildType
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

/**
 * Tres ambientes:
 *
 *  debug    — desenvolvimento local, sem ofuscacao, id sufixado para conviver
 *             com os outros no mesmo aparelho.
 *  staging  — build de homologacao com R8 LIGADO. Existe para que os problemas
 *             de shrinking/ofuscacao aparecam antes da release, e nao depois.
 *  release  — producao: R8 + shrinkResources + remocao dos logs.
 */
internal fun Project.configureApplicationBuildTypes(extension: ApplicationExtension) {
    extension.apply {
        buildFeatures.buildConfig = true

        buildTypes {
            debug {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-debug"
                isMinifyEnabled = false
                buildConfigField("String", "ENVIRONMENT", "\"debug\"")
            }

            release {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
                buildConfigField("String", "ENVIRONMENT", "\"release\"")
            }

            create("staging") {
                initWith(getByName("release"))
                applicationIdSuffix = ".staging"
                versionNameSuffix = "-staging"
                isDebuggable = false
                matchingFallbacks += "release"
                signingConfig = signingConfigs.getByName("debug")
                buildConfigField("String", "ENVIRONMENT", "\"staging\"")
            }
        }
    }
}

/**
 * Modulos de biblioteca precisam declarar `staging` tambem, senao o Gradle nao
 * encontra a variante equivalente ao resolver a dependencia do `:app`.
 */
internal fun Project.configureLibraryBuildTypes(extension: LibraryExtension) {
    extension.apply {
        buildTypes {
            create("staging") {
                initWith(getByName("release"))
                matchingFallbacks += "release"
            }
        }
    }
}

internal val ApplicationBuildType.isStaging: Boolean
    get() = name == "staging"
