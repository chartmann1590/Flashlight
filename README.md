# Flashlight

Modern Android flashlight app (Kotlin + Compose) with Firebase telemetry, AdMob monetization, signed release automation, and a published GitHub Pages website.

## Website

- Live site: https://chartmann1590.github.io/Flashlight/
- In-app link: `About Website` button in `MainActivity`

## Support the developer

- **Buy Me a Coffee:** https://buymeacoffee.com/charleshartmann  
- Also linked from the app (main screen) and the GitHub Pages site.

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

- **GitHub Releases (recommended):** each successful run on `main` creates a **real GitHub Release** (not tag-only) with **two attached assets**: a signed `.apk` and a signed `.aab` (Play Store bundle). The release is marked **Latest** and uses a unique tag `v<versionName>-ci<run_id>`.  
  **Latest download:** https://github.com/chartmann1590/Flashlight/releases/latest  

- **Workflow artifacts:** each run also uploads a zip on the Actions run summary page.

- **Manual GitHub Release:** when you publish a release from the UI (`release` event), the workflow attaches the same `app-release.apk` and `app-release.aab` to that release.

Build outputs (paths inside the repo / runner):

- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

Release guard:

- CI fails if release `BuildConfig` does not contain `ADS_ENABLED = true`

Play Store versioning (automatic on CI):

- On GitHub Actions, **`versionCode` = `2_000_000 + GITHUB_RUN_NUMBER`** so every workflow run produces a strictly higher code than the last (required for Play uploads).
- **`versionName`** defaults to **`2.0.<run number>`** on CI, or set explicitly with env `CI_VERSION_NAME`.
- Override either value in a workflow if needed: **`CI_PLAY_VERSION_CODE`** (int) or **`CI_VERSION_NAME`** (string).
- Local/Android Studio builds keep **`versionCode` 3** and **`versionName` 2.0** when those CI env vars are unset.

### GitHub Pages deployment

Workflow: `.github/workflows/pages.yml`

- Deploys `docs/` to GitHub Pages on push to `main`

### E2E workflow

Workflow: `.github/workflows/e2e.yml`

- Runs Playwright end-to-end checks against the live website
- Verifies page title, main call-to-action links, and screenshot assets
