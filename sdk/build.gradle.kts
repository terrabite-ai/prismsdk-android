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
    id("maven-publish")
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
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

// `./gradlew :sdk:publishToMavenLocal` for a local install, and
// `./gradlew :sdk:assembleRelease` for the AAR that goes on a GitHub release.
// Maven Central publication waits for the `ai.terrabite` namespace to be
// verified on Sonatype Central.
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.property("GROUP") as String
            artifactId = project.property("POM_ARTIFACT_ID") as String
            version = project.property("VERSION_NAME") as String
            afterEvaluate { from(components["release"]) }
            pom {
                name.set("Prism SDK")
                description.set("Background location tracking for Android apps.")
                url.set("https://github.com/terrabite-ai/prismsdk-android")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        name.set("Terrabite AI")
                        email.set("support@terrabite.ai")
                    }
                }
            }
        }
    }
}
