import com.mej.gradle.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * Receita unica de modulo de feature: biblioteca Android, Compose, Koin,
 * ViewModel e as dependencias de core que toda tela consome.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("rm.android.library")
        pluginManager.apply("rm.android.compose")

        dependencies {
            add("implementation", project(":domain"))

            add("api", libs.findLibrary("core-designsystem").get())
            add("api", libs.findLibrary("core-mvi").get())
            add("api", libs.findLibrary("core-common").get())

            add("implementation", libs.findLibrary("androidx-core-ktx").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("koin-androidx-compose").get())
            add("implementation", libs.findLibrary("androidx-compose-material-icons").get())
            add("implementation", libs.findLibrary("coil-compose").get())

            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("mockk").get())
            add("testImplementation", libs.findLibrary("turbine").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())

            add("androidTestImplementation", libs.findLibrary("androidx-test-junit").get())
            add("androidTestImplementation", libs.findLibrary("compose-ui-test").get())
            add("androidTestImplementation", libs.findLibrary("compose-ui-test-junit4").get())
            add("debugImplementation", libs.findLibrary("compose-ui-test-manifest").get())
        }
    }
}
