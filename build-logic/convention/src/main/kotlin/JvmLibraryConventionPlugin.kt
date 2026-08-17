import com.mej.gradle.configureDetekt
import com.mej.gradle.configureKotlinJvm
import com.mej.gradle.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * Modulo Kotlin puro. Usado em `:core:domain` e `:core:common` para que o
 * dominio nao consiga sequer importar `android.*` — a regra de Clean
 * Architecture passa a ser garantida pelo compilador, nao por disciplina.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        pluginManager.apply("jacoco")

        configureKotlinJvm()
        configureDetekt()

        dependencies {
            add("testImplementation", libs.findLibrary("junit").get())
        }

        tasks.withType<Test>().configureEach { useJUnit() }
    }
}
