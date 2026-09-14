# Changelog

All notable changes to Prism SDK for Android. This project follows
[Semantic Versioning](https://semver.org/spec/v2.0.0.html). Below 1.0.0, a
minor version may change the API.

## [0.1.0] — unreleased

First release.

### Added

- `Prism`, the entry point: initialise with an API key, start and stop
  tracking.
- Background location tracking in a foreground service, with visit detection.
  `PrismLocation` carries the fix and the device and app state when it was
  taken.
- `PrismConfig` for tuning tracking, the horizontal-accuracy threshold, and the
  foreground-service notification. Kotlin constructor or Java `Builder`.
- Delivery by `PrismLocationListener` / `PrismErrorListener`, or by Kotlin
  `Flow` (`locations()`, `errors()`).
- Permission handling: foreground and background requests through an
  `Activity`, and `PrismPermissionStatus` for the full state without prompting,
  including whether the grant is precise.
- User identity and metadata attached to every location.
- Battery-optimisation helpers.
- `PrismLocation.toMap()` with snake_case wire names, for bridged hosts.
- Kotlin and Java. Android 7.0 (API 24) or later.

### Notes

- Tracking only. Locations are delivered to your app and nowhere else.
- Listeners are called on the engine's thread, not the main thread.
