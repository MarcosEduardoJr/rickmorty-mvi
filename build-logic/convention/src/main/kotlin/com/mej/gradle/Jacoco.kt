package com.mej.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Codigo gerado nao diz nada sobre a qualidade dos testes, entao Hilt, Room,
 * Compose e afins ficam fora do calculo — senao a cobertura vira um numero
 * inflado e inutil.
 */
private val COVERAGE_EXCLUSIONS = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*_Hilt*.class",
    "**/Hilt_*.class",
    "**/*_Factory*.*",
    "**/*_MembersInjector*.*",
    "**/Dagger*Component*.*",
    "**/*Module_*Factory.*",
    "**/*_Impl*.*",
    "**/ComposableSingletons*.*",
    "**/*Preview*.*",
    "**/databinding/**",
)

internal fun Project.configureJacoco(variant: String = "debug") {
    pluginManager.apply("jacoco")

    extensions.configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
    }

    tasks.withType<Test>().configureEach {
        extensions.configure<JacocoTaskExtension> {
            // Obrigatorio para instrumentar bytecode gerado pelo compilador Kotlin.
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }

    val testTaskName = "test${variant.replaceFirstChar { it.uppercase() }}UnitTest"

    tasks.register<JacocoReport>("jacocoTestReport") {
        group = "verification"
        description = "Relatorio de cobertura das unit tests do modulo."

        val testTask = tasks.named<Test>(testTaskName)
        dependsOn(testTask)

        reports {
            html.required.set(true)
            xml.required.set(true)
            csv.required.set(false)
        }

        val kotlinClasses = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/$variant") {
            exclude(COVERAGE_EXCLUSIONS)
        }
        val javaClasses = fileTree("${layout.buildDirectory.get()}/intermediates/javac/$variant") {
            exclude(COVERAGE_EXCLUSIONS)
        }

        classDirectories.setFrom(kotlinClasses, javaClasses)
        sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
        // Aponta para o .exec da propria task de teste. Varrer o build inteiro
        // faria o Gradle acusar dependencia implicita com outputs do AGP.
        executionData.setFrom(
            testTask.map { it.extensions.getByType(JacocoTaskExtension::class.java).destinationFile!! },
        )
    }
}
