import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// The API key stays in the developer's local.properties file and is not committed.
val localSecrets = Properties().apply {
    val secretsFile = rootProject.file("local.properties")
    if (secretsFile.exists()) secretsFile.inputStream().use { load(it) }
}
// Read secrets from local.properties first, then Gradle properties / environment.
// This supports Android Studio builds and GitHub Actions without committing API keys.
fun secret(name: String): String = sequenceOf(
    localSecrets.getProperty(name),
    providers.gradleProperty(name).orNull,
    System.getenv(name)
).firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()
val orsApiKey = secret("ORS_API_KEY")
val cartoApiKey = secret("CARTO_API_KEY")
val googleMapsJavaScriptApiKey = sequenceOf(
    localSecrets.getProperty("GOOGLE_STREETVIEW_API_KEY"),
    localSecrets.getProperty("GOOGLE_MAPS_API_KEY"),
    providers.gradleProperty("GOOGLE_STREETVIEW_API_KEY").orNull,
    providers.gradleProperty("GOOGLE_MAPS_API_KEY").orNull,
    System.getenv("GOOGLE_STREETVIEW_API_KEY"),
    System.getenv("GOOGLE_MAPS_API_KEY")
).firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()
val mapillaryAccessToken = sequenceOf(
    localSecrets.getProperty("MAPILLARY_ACCESS_TOKEN"),
    providers.gradleProperty("MAPILLARY_ACCESS_TOKEN").orNull,
    System.getenv("MAPILLARY_ACCESS_TOKEN")
).firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()

android {
    namespace = "com.czerman.haulway_navigation"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.czerman.haulway_navigation"
        minSdk = 26
        targetSdk = 37
        versionCode = 18
        versionName = "2.1.4-vision14-native-map-globe"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "ORS_API_KEY", "\"${orsApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        buildConfigField("String", "CARTO_API_KEY", "\"${cartoApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${googleMapsJavaScriptApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        buildConfigField("String", "MAPILLARY_ACCESS_TOKEN", "\"${mapillaryAccessToken.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("org.maplibre.gl:android-sdk-opengl:2026.10.19-globe")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}