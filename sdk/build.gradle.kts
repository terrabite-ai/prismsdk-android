// Prism SDK for Android: the `:sdk` module is the published library.
//
// The tracking engine is `com.localsdk:core`, a normal Maven Central dependency.
// It is `implementation`, not `api`, so nothing from `com.localsdk` reaches an
// integrator's compile classpath: they see `ai.terrabite.prism` and nothing else.
// Coroutines is `api` because `Prism.locations()` returns a `Flow`, which the
// integrator has to be able to name.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "ai.terrabite.prism"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "SDK_VERSION", "\"${project.property("VERSION_NAME")}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        // Robolectric needs the manifest and resources of the module under test.
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.localsdk.core)
    implementation(libs.androidx.core)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Publishing, via com.vanniktech.maven.publish — the same plugin the engine
// uses. Everything is configured from gradle.properties: coordinates (GROUP,
// POM_ARTIFACT_ID, VERSION_NAME), POM fields (POM_*), the target
// (SONATYPE_HOST) and signing (RELEASE_SIGNING_ENABLED). No DSL block here,
// because the plugin finalises those properties once it has read them.
// The plugin adds the sources and javadoc jars and the signatures Maven
// Central validates.
//
//   ./gradlew :sdk:publishToMavenLocal    ~/.m2, no signing needed
//   ./gradlew :sdk:publishToMavenCentral  Central Portal; needs the release
//                                         machine's ~/.gradle/gradle.properties
//                                         to hold mavenCentralUsername/Password
//                                         and the signing.* keys
