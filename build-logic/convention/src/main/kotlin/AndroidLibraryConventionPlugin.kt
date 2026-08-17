import com.android.build.api.dsl.LibraryExtension
import com.mej.gradle.configureAndroid
import com.mej.gradle.configureDetekt
import com.mej.gradle.configureJacoco
import com.mej.gradle.configureLibraryBuildTypes
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<LibraryExtension> {
            configureAndroid(this)
            configureLibraryBuildTypes(this)

            defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            testOptions.unitTests.isReturnDefaultValues = true

            // Modulo de biblioteca nao publica BuildConfig sem necessidade.
            buildFeatures.buildConfig = false
        }

        configureDetekt()
        configureJacoco()
    }
}
