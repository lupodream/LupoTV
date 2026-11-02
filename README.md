# LUPO PLAYER — Android TV / Fire TV starter project

This is the first native Android project scaffold for the LUPO PLAYER concept, using the supplied wolf logo and black/gold styling.

## Included in this first prototype
- Android TV / Fire TV launcher entry points
- Remote-friendly landscape layout and large focusable buttons
- Black and gold home screen with the supplied logo
- Xtream Codes form (server URL, username, password)
- M3U URL form
- Local document picker for selecting a playlist file

## Not implemented yet
This is a UI starter, not a finished IPTV player. Authentication, playlist parsing, EPG, channel/movie browsing, and video playback still need to be implemented and tested with a lawful stream source. The current Connect/Load buttons only validate that fields are filled in.

## Build
1. Open this folder in Android Studio (current stable version).
2. Allow Gradle sync and install the Android SDK Platform 35 if prompted.
3. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Test on an Android TV device or Fire TV device. Fire TV installation may require enabling app installation from the chosen developer source.

Application ID: `com.lupoplayer.app`
Version: `1.0` (prototype)

## Cloud build from iPhone (GitHub Actions)
1. Create a new repository on GitHub (for example `lupo-player`).
2. Upload the contents of this ZIP's `lupo-player` folder into the repository root.
3. Upload `.github/workflows/android.yml`, preserving that folder path.
4. In GitHub, open **Actions** → **Build LUPO PLAYER APK** → **Run workflow**.
5. When the run finishes, download the artifact named `lupo-player-debug-apk`. It contains `app-debug.apk`.

This produces a debug/test APK, not a signed production release. The current app is a starter interface: login fields and file selection are scaffolding; playlist parsing, provider connection, EPG and video playback still need implementation. Only use playlists and streams you are authorized to access.
