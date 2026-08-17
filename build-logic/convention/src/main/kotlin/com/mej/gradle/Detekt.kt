package com.mej.gradle

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * Analise estatica com uma configuracao unica na raiz, aplicada a todo modulo.
 * `detekt-formatting` traz as regras do ktlint, cobrindo estilo e lint no
 * mesmo comando.
 */
internal fun Project.configureDetekt() {
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        baseline = rootProject.file("config/detekt/baseline.xml").takeIf { it.exists() }
    }

    dependencies {
        add("detektPlugins", libs.findLibrary("detekt-formatting").get())
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = AndroidConfig.JVM_TARGET.target
        reports {
            html.required.set(true)
            xml.required.set(true)
            sarif.required.set(false)
            txt.required.set(false)
            md.required.set(false)
        }
    }
}
