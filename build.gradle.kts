// Root build file. Plugins are declared here with `apply false` so every module
// resolves the same versions from gradle/libs.versions.toml.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}
