import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

fun escapedBuildConfigString(value: String): String {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) {
        file.inputStream().use { load(it) }
    }
}

fun configuredValue(name: String): String {
    return providers.gradleProperty(name).orNull
        ?.takeIf { it.isNotBlank() }
        ?: localProperties.getProperty(name)
            ?.takeIf { it.isNotBlank() }
        ?: providers.environmentVariable(name).orNull.orEmpty()
}

val supabaseUrl = configuredValue("SUPABASE_URL")
val supabaseAnonKey = configuredValue("SUPABASE_ANON_KEY")

android {
    namespace = "com.memoria.app"
    compileSdk = 36

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.memoria.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "BACKEND_BASE_URL", "\"http://10.0.2.2:8080/api\"")
        buildConfigField("String", "SUPABASE_URL", escapedBuildConfigString(supabaseUrl))
        buildConfigField("String", "SUPABASE_ANON_KEY", escapedBuildConfigString(supabaseAnonKey))
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))
    implementation("androidx.activity:activity-compose:1.12.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.17.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
