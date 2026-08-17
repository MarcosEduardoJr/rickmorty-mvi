plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.apollo) apply false
    alias(libs.plugins.detekt)
}

val codeModules = subprojects.filter { it.buildFile.exists() }

tasks.register("detektAll") {
    group = "verification"
    description = "Analise estatica em todos os modulos."
    dependsOn(codeModules.map { "${it.path}:detekt" })
}

tasks.register("coverageAll") {
    group = "verification"
    description = "Relatorio de cobertura de todos os modulos."
    dependsOn(codeModules.map { "${it.path}:jacocoTestReport" })
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
