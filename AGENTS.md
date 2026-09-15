# prismsdk-android

This file orients AI coding agents working on this repo. `CLAUDE.md` is a
symlink to this file — edit `AGENTS.md` only.

## Project Overview

Prism SDK for Android: a Kotlin wrapper over the LocalSDK tracking engine.
Integrators add one Gradle dependency, `import ai.terrabite.prism.*`, and see
only Prism's types. The engine is `com.localsdk:core` from Maven Central; its
source is in a separate private repository, `localsdk/localsdk-android-core`,
and is not here.

## Repo layout

```
sdk/                                  the published library, module :sdk
sdk/src/main/java/ai/terrabite/prism/ source. Prism.kt is the only entry point.
sdk/src/test/java/ai/terrabite/prism/ JUnit 4 + Robolectric. One Java test on purpose.
gradle/libs.versions.toml             versions; track the engine where they overlap
gradle.properties                     GROUP / POM_ARTIFACT_ID / VERSION_NAME
```

No example app yet.

## Build & Test

```bash
./gradlew :sdk:assembleRelease       # AAR at sdk/build/outputs/aar/sdk-release.aar
./gradlew :sdk:testDebugUnitTest     # unit tests, ~30 s
./gradlew :sdk:publishToMavenLocal   # ai.terrabite:prismsdk:<VERSION_NAME> in ~/.m2
```

Always run the tests before committing. Needs `local.properties` with
`sdk.dir=` pointing at an Android SDK (git-ignored). JDK 17 or 22 both work.

## Rules

**Prism's public API never names an engine type.** `com.localsdk.*` appears
only in `internal` mapping functions (`PrismLocation.from`, `PrismConfig.toEngine`,
the enum `from`/`toEngine` pairs) and in `Prism`'s initialiser. Check with:

```bash
grep -rn "com.localsdk" sdk/src/main | grep -v -E "^\S+:\s*import |internal "
```

The engine is an `implementation` dependency, never `api`. Coroutines is `api`
because `Prism.locations()` returns a `Flow`.

**Prism is tracking-only.** `PrismConfig.toEngine()` pins `setPublishEnabled(false)`
and `PrismConfigMappingTest` asserts it. Do not expose publishing. Decided
2026-09-14, matching the iOS SDK.

**The engine's listener slots are single and Prism owns them.** `Prism`'s
`init` registers one location and one error listener with the engine, once.
`PrismDispatcher` fans out to the Prism listener and the flows. Never call
`LocalSDK.onLocation` / `LocalSDK.onError` anywhere else.

**The engine's `Config` is a global.** Its builder mutates one object in place.
`PrismConfig` is an immutable value; `toEngine()` writes every field on every
apply so nothing stale survives. Tests that touch it restore defaults in `@After`.

**Every public member of `Prism` is `@JvmStatic`**, and listeners are
`fun interface`s. `JavaUsageTest.java` compiles the Java form; keep it passing.

**Adding a field to `PrismLocation` means three edits**: the data class,
`from()`, and `toMap()`; then bump the 27 in `PrismLocationMappingTest`. The
count test fails first if the engine's `Location` grows.

**Engine internals stay private.** Distance tables, speed thresholds, filter
rules and region sizing live in the engine's private source. Describe tracking
modes only in relative terms (more or fewer updates, more or less battery).

**Versions track the engine.** AGP, Kotlin and coroutines in the version
catalog match `localsdk-android-core` 1.0.0. Bump them together.

## Threading

The engine invokes its listener synchronously on whatever thread produced the
fix — not the main thread. Prism passes that through unchanged and documents
it on `PrismLocationListener`. The flows use `extraBufferCapacity = 1` with
`DROP_OLDEST`, so a slow collector gets the newest value and `tryEmit` never
suspends the engine's thread.

## Permissions

Requests go through `ActivityCompat.requestPermissions` with request codes
1000 (foreground) and 1001 (background), exposed as constants on `Prism`. The
engine offers no callback; the host handles `onRequestPermissionsResult`.
`PrismPermissionStatus.of(context)` reads the state without prompting.

## Releasing

`VERSION_NAME` in `gradle.properties` is the version and the git tag. A
release is:

1. the tag;
2. `./gradlew :sdk:publishToMavenCentral` from a machine whose
   `~/.gradle/gradle.properties` holds `mavenCentralUsername`,
   `mavenCentralPassword` (a Central Portal user token) and the `signing.*`
   keys — never commit any of those;
3. a GitHub release carrying the same AAR as `prismsdk-<version>.aar`, for
   anyone who cannot wait for Central.

Publishing is configured entirely in `gradle.properties` (`SONATYPE_HOST`,
`SONATYPE_AUTOMATIC_RELEASE`, `RELEASE_SIGNING_ENABLED`, `POM_*`); do not add a
`mavenPublishing {}` block to the build file, the plugin rejects a second
configuration. `./gradlew :sdk:publishToMavenLocal` is the dry run: it must
produce the AAR, sources jar, javadoc jar, module, POM and `.asc` signatures.

## Open decisions

- Engine version. Wraps the published 1.0.0. The engine's local branch has two
  unreleased crash fixes; bump `localsdk` in the catalog when the engine ships.
- An example app and `docs/`, as on iOS. Deferred.
