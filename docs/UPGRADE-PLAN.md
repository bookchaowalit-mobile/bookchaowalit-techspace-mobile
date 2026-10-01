# Upgrade plan

## Current state

Score: 5/10 (was 4/10) — real, tested KMP shared module with honest CI; no
Android/iOS UI or data source yet.

## Backlog

- P0: Add an `androidApp` Compose module that consumes `:shared` (needs Android
  SDK / Google Maven in the build environment).
- P1: Add an `iosApp` SwiftUI project that links the `shared` framework.
- P1: Load the catalog from the Techspace frontend/API (Ktor client in
  `commonMain`, fake for tests); persist bookmarks.
- P2: Pin `distributionSha256Sum` in `gradle-wrapper.properties`.

## Done in this pass (pass 1)

- Replaced the placeholder `AppInfo` with catalog/search/bookmark domain logic + 8 commonTest tests.
- Committed Gradle wrapper (8.10.2); Kotlin 2.0.0 -> 2.0.21 (patch) for Gradle 8.10 support.
- Added a `jvm()` target so tests run anywhere; dropped the unused Android
  Gradle Plugin and coroutines dependency and the non-existent `:androidApp` include.
- CI: removed `|| true`; runs `:shared:build` (Linux) and iOS simulator tests (macOS).
- README now describes what actually exists.

## Done in this pass (pass 2)

- Fixed search: a blank tag in the filter (empty chip) made every search return nothing; blank resource tags showed up as an empty tag in `tagCounts`; `##kotlin` normalised to `#kotlin`.
- Title ordering is now case-insensitive with an id tie-breaker (was: "apple" sorted after "Zebra").
- 9 more commonTest cases (17 total). Verified with `./gradlew :shared:jvmTest --offline` and `:shared:compileCommonMainKotlinMetadata` (iOS targets need macOS; not run).

## Done in this pass (pass 3)

- Blank checks now treat zero-width characters (U+200B/U+200C/U+200D/U+2060/BOM/soft hyphen) as blank: a `"​"` title/id was accepted and a zero-width-only query hid every resource.
- Tags and queries split on any Unicode whitespace: `"Machine Learning"` (NBSP) normalised to one odd tag instead of `machine-learning`.
- URL host check strips userinfo/port and rejects control chars: `https://:8080/`, `https://user@/` and tab-containing hosts were accepted.
- `readingMinutes(Int.MAX_VALUE)` overflowed to a negative value.
- 5 regression tests (22 total), verified with `./gradlew :shared:jvmTest --offline`.
