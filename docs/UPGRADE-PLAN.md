# Upgrade plan

## Current state

Score: 4/10 (was 1/10) — real, tested KMP shared module with honest CI; no
Android/iOS UI or data source yet.

## Backlog

- P0: Add an `androidApp` Compose module that consumes `:shared` (needs Android
  SDK / Google Maven in the build environment).
- P1: Add an `iosApp` SwiftUI project that links the `shared` framework.
- P1: Load the catalog from the Techspace frontend/API (Ktor client in
  `commonMain`, fake for tests); persist bookmarks.
- P2: Pin `distributionSha256Sum` in `gradle-wrapper.properties`.

## Done in this pass

- Replaced the placeholder `AppInfo` with catalog/search/bookmark domain logic + 8 commonTest tests.
- Committed Gradle wrapper (8.10.2); Kotlin 2.0.0 -> 2.0.21 (patch) for Gradle 8.10 support.
- Added a `jvm()` target so tests run anywhere; dropped the unused Android
  Gradle Plugin and coroutines dependency and the non-existent `:androidApp` include.
- CI: removed `|| true`; runs `:shared:build` (Linux) and iOS simulator tests (macOS).
- README now describes what actually exists.
