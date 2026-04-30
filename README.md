# Flashlight

Modern Android flashlight app (Kotlin + Compose) with Firebase telemetry, AdMob monetization, signed release automation, and a published GitHub Pages website.

## Website

- Live site: https://chartmann1590.github.io/Flashlight/
- In-app link: `About Website` button in `MainActivity`

## Workflow Status

[![Android Signed Release](https://github.com/chartmann1590/Flashlight/actions/workflows/android-release.yml/badge.svg)](https://github.com/chartmann1590/Flashlight/actions/workflows/android-release.yml)
[![Deploy GitHub Pages](https://github.com/chartmann1590/Flashlight/actions/workflows/pages.yml/badge.svg)](https://github.com/chartmann1590/Flashlight/actions/workflows/pages.yml)
[![E2E Website Tests](https://github.com/chartmann1590/Flashlight/actions/workflows/e2e.yml/badge.svg)](https://github.com/chartmann1590/Flashlight/actions/workflows/e2e.yml)

## App Highlights

- Torch toggle with always-on interstitial trigger when turning ON
- Firebase Crashlytics + Performance Monitoring
- AdMob integration (release build includes ads enabled guard)
- Signed APK/AAB GitHub release pipeline

## CI/CD

### Signed Android release

Workflow: `.github/workflows/android-release.yml`

Required repository secrets:

- `RELEASE_KEYSTORE_B64` (base64-encoded keystore)
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Where to download signed builds:

- **GitHub Releases (recommended):** each successful run on `main` publishes a rolling prerelease **`ci-signed-latest`** with the signed APK and AAB attached:  
  https://github.com/chartmann1590/Flashlight/releases/tag/ci-signed-latest  
  (Older runs are also kept as a **workflow artifact** zip on the Actions run page.)

- **Official versioned releases:** when you publish a GitHub Release from the UI, the same workflow attaches `app-release.apk` and `app-release.aab` to that release (`release` event).

Build outputs (paths inside the repo / runner):

- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

Release guard:

- CI fails if release `BuildConfig` does not contain `ADS_ENABLED = true`

### GitHub Pages deployment

Workflow: `.github/workflows/pages.yml`

- Deploys `docs/` to GitHub Pages on push to `main`

### E2E workflow

Workflow: `.github/workflows/e2e.yml`

- Runs Playwright end-to-end checks against the live website
- Verifies page title, main call-to-action links, and screenshot assets
