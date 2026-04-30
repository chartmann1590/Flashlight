# Flashlight

Android flashlight app with release CI.

## CI secrets

Set these repository secrets:

- `RELEASE_KEYSTORE_B64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Workflow: `.github/workflows/android-release.yml`
# Flashlight

Modern Android flashlight app (Kotlin + Compose) with:

- Torch, strobe, SOS, and screen-light modes
- Firebase Crashlytics + Performance instrumentation
- AdMob banner/interstitial/native ads
- Home-screen widget

## CI Release Workflow

GitHub Actions workflow: `.github/workflows/android-release.yml`

To produce signed release artifacts in CI, configure repository secrets:

- `RELEASE_KEYSTORE_B64`: base64-encoded keystore file
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

When run, CI generates:

- Signed APK: `flashlight-release.apk`
- Signed AAB: `flashlight-release.aab`

Tag a release as `v*` to auto-create a GitHub Release with attached artifacts.

## GitHub Pages

Pages content is in `docs/` and deployed by `.github/workflows/pages.yml`.
