import com.android.build.api.dsl.ApplicationExtension
import com.mej.gradle.AndroidConfig
import com.mej.gradle.configureAndroid
import com.mej.gradle.configureApplicationBuildTypes
import com.mej.gradle.configureDetekt
import com.mej.gradle.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<ApplicationExtension> {
            configureAndroid(this)
            configureApplicationBuildTypes(this)

            defaultConfig {
                targetSdk = AndroidConfig.TARGET_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            testOptions.unitTests.isReturnDefaultValues = true
        }

        configureDetekt()
        configureJacoco()
    }
}
