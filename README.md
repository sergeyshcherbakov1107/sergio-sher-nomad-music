# SERGIO SHER NOMAD MUSIC v0.3

Android source project (Kotlin / Jetpack Compose / Media3).

## Added in v0.3
- MediaSessionService-based playback for background audio and lock-screen controls
- Seek bar and elapsed/total time
- Playback error feedback and automatic audio focus handling
- Cloudflare R2 cover and initial audio URL

## Build
Open SSN_APP in Android Studio with JDK 17 and Android SDK 35. Allow Gradle sync and choose Build > Build APK(s). The ZIP does not include a compiled APK or Gradle wrapper. No compilation or device testing was possible in this environment.

## Audio verification
The cover URL was confirmed by the user. The audio object VN20261109_075848.m4a was carried over from v0.2 but its public accessibility has not been verified. If playback returns 404, replace the audio URI in MainActivity.kt with the exact public R2 object URL.

## Scope
This is a single-track MVP. Offline downloads, catalog API, lyrics sync, and search are not yet implemented.
