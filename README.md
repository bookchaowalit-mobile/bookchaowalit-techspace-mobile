# Techspace — Mobile

Kotlin Multiplatform (KMP) shared module for **Techspace**, a curated catalog
of tech learning resources (articles, videos, tools, courses).

Part of [Chaowalit Greepoke](https://bookchaowalit.com)'s 101 Portfolio Projects.

## What is here today

Only the shared business-logic module exists. There is **no Android or iOS UI
project yet** — see [docs/UPGRADE-PLAN.md](docs/UPGRADE-PLAN.md).

`shared/` (package `com.bookchaowalit.techspace`):

- `Resources.kt` — `TechResource` model with validation (http(s) URLs only,
  non-blank title), tag normalisation, reading-time estimate
- `Catalog.kt` — de-duplicated catalog with
  - weighted search (title > tag > summary)
  - filters by kind, level and required tags
  - sort by relevance, newest or shortest
  - tag usage counts and ordered bookmarks

Tests live in `shared/src/commonTest` and run on every target.

## Tech Stack

- **Language:** Kotlin 2.0 (Kotlin Multiplatform)
- **Targets:** JVM (tests, and consumable by an Android app module),
  iosX64 / iosArm64 / iosSimulatorArm64 (static `shared` framework)
- **Build:** Gradle 8.10 wrapper (committed)

## Getting Started

Requires JDK 17+.

```bash
./gradlew :shared:jvmTest   # fast unit tests on the JVM
./gradlew :shared:build     # full build; iOS targets are skipped on non-macOS hosts
./gradlew :shared:iosSimulatorArm64Test   # macOS only
```

CI (`.github/workflows/build.yml`) runs `:shared:build` on Ubuntu and the iOS
simulator tests on macOS. Failures fail the workflow.

## Related

- **Frontend:** [bookchaowalit-website/techspace-frontend](https://github.com/bookchaowalit-website/bookchaowalit-techspace-frontend)
- **Portfolio:** [bookchaowalit.com](https://bookchaowalit.com)

## License

MIT
