# Later Box

**Save Now. Organize Easily. Enjoy Later.**

Later Box is a personal content-saving, bookmarking, playlist management, and read/watch-later Android application. It allows users to save content seamlessly from any Android app using native Android Sharing — including Instagram, YouTube, TikTok, Reddit, X (Twitter), Facebook, web browsers, and news apps.

---

## Key Features

- **Native Android Share Target (`ACTION_SEND` & `ACTION_SEND_MULTIPLE`)**:
  - Share single links, web pages, videos, images, notes, or multiple files directly from external apps.
  - Quick bottom sheet offering **Save Directly to Inbox** or **Choose Playlists** on the fly.
  - Automatically returns user to the source application after saving.
- **Smart Metadata Extraction**:
  - Automatically fetches OpenGraph page title, description, thumbnail/preview image, domain, and favicon.
  - Asynchronous background extraction ensures saves are instant and never blocked by network latency or blocked sites.
- **Dashboard-First Home Experience**:
  - Contextual header with time-based greetings and quick settings.
  - Global Search bar covering titles, URLs, domains, and personal notes.
  - **Continue Watching / Reading** section showing partially consumed content.
  - **Your Playlists** quick carousel with one-tap new playlist creation.
  - **Recent Saves** feed with quick actions.
- **Complete Library with Grid & List Views**:
  - Interactive switch between visual Grid cards and informative List cards.
  - Filter by content type: All, Articles, Videos, Images, Audio, Documents, Links, Notes.
  - Filter by status: All, Favorites, Archive.
  - Sorting options: Newest First, Oldest First, Title (A-Z), Recently Opened.
- **Playlist & Label Management**:
  - Many-to-many relationship: a saved item can belong to multiple playlists without duplicating storage.
  - Default "Inbox" collection for quick unassigned saves.
  - Custom playlist icons, item count badges, and in-playlist search.
- **In-App Viewing & Safe External Launching**:
  - In-app web reader with loading progress for articles and web content.
  - High-performance video player powered by AndroidX Media3 (ExoPlayer).
  - High-definition image viewer with Coil.
  - One-tap "Open in Original App" with safe intent fallbacks to external browsers.
- **Trash & Data Recovery**:
  - Soft-delete to Recently Deleted trash with instant restore or permanent purge.
- **Privacy & Local-First Architecture**:
  - 100% on-device storage using Android Jetpack Room and DataStore.
  - Zero cloud lock-in, zero tracking, zero mandatory account creation.
- **Design System & Theme**:
  - Pure monochrome high-contrast visual identity (White Background & Solid Black):
    - **Background**: Pure White (`#FFFFFF`)
    - **Cards & Surfaces**: Clean Pure White (`#FFFFFF`)
    - **Primary Typography & Actions**: Solid Black (`#000000`)
    - **Secondary Typography**: Neutral Grey (`#555555`)
    - **Tertiary Metadata**: Muted Grey (`#888888`)
    - **Borders & Dividers**: Light Neutral Outline (`#E5E7EB`)
    - **Inactive Filter Pills**: Subtle Light Grey (`#F4F4F5`)
    - **Active View & Filter Toggle**: Solid Black (`#000000`) with White text (`#FFFFFF`)
    - **Dark Theme Adaptive**: Deep Black (`#000000`) & Dark Surfaces (`#121212` / `#1E1E1E`)
  - Typography: High-contrast editorial Serif headings paired with clean modern Inter/Poppins body and microcopy.
  - Strictly ZERO yellow, zero amber, zero brown/ochre (`#9E6F21` / `#9C6E1E`), strictly zero red font anywhere in the app.
  - Full support for Dynamic System Theme, Light Mode, and Dark Mode.

---

## Architecture & Technology Stack

- **UI**: 100% Jetpack Compose with Material 3, Navigation Compose, and custom theme tokens.
- **Persistence**: Room Database with normalized schema, TypeConverters, ForeignKeys (`CASCADE`), and Junction relations.
- **Preferences**: Jetpack DataStore Preferences.
- **Networking**: OkHttp with custom HTML OpenGraph and favicon parser.
- **Media**: Coil Compose with VideoFrameDecoder; AndroidX Media3 (ExoPlayer) for video playback.
- **Architecture Pattern**: MVVM with Repository Pattern, Kotlin Coroutines, and StateFlow.

---

## Build & Test Instructions

### Prerequisites
- JDK 17 (Microsoft OpenJDK 17 or equivalent)
- Android SDK 35 (minSdk: 24, targetSdk: 35)
- Gradle 8.10.2 (included via `./gradlew`)

### Running Tests
```bash
./gradlew test
```

### Running Lint
```bash
./gradlew lintDebug
```

### Assembling Debug APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Assembling Release APK
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.
