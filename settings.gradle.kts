pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Only for developing against an Enrich build that Central does not
        // have yet: ./gradlew -Pprism.useMavenLocal=true ...
        if (providers.gradleProperty("prism.useMavenLocal").orNull == "true") {
            mavenLocal()
        }
    }
}

rootProject.name = "prismsdk-android"
include(":sdk")
