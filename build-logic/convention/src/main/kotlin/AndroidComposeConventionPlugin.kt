import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.mej.gradle.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Habilita Compose e injeta o BOM, que e o unico ponto onde as versoes das
 * bibliotecas de Compose sao decididas.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val extension: CommonExtension<*, *, *, *, *, *> =
            extensions.findByType(ApplicationExtension::class.java)
                ?: extensions.getByType(LibraryExtension::class.java)

        extension.buildFeatures.compose = true

        dependencies {
            val bom = platform(libs.findLibrary("androidx-compose-bom").get())
            add("implementation", bom)
            add("androidTestImplementation", bom)

            add("implementation", libs.findLibrary("androidx-compose-ui").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("implementation", libs.findLibrary("androidx-compose-material3").get())

            add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
        }
    }
}
