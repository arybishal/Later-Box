# Later Box v1.0.0

**Later Box — Save Now. Organize Easily. Enjoy Later.**

Later Box is a personal content-saving, bookmarking, playlist management, and read/watch-later Android application.

---

### Highlights & New Features

- **Native Android Share Target (`ACTION_SEND` & `ACTION_SEND_MULTIPLE`)**:
  - Registered intent filters for URLs, text, images, videos, audio, documents, and multiple shared files.
  - Quick bottom sheet interface supporting **Save to Inbox** or **Choose Playlists**.
  - Background asynchronous metadata extraction and automatic return to host applications (Instagram, YouTube, TikTok, Reddit, X, web browsers).
- **Smart Metadata Extraction Engine**:
  - Automatic extraction of OpenGraph metadata (title, description, preview thumbnail, website domain, favicon).
  - Resilient offline fallbacks with timeout management.
- **Dashboard-First Home Experience**:
  - Contextual header with time-based greetings and quick settings.
  - Prominent Global Search field.
  - "Continue" section highlighting saved content with consumption progress.
  - "Your Playlists" carousel with instant playlist creation.
  - "Recent Saves" feed with quick action menus.
- **Complete Library with Dual View Layouts**:
  - Grid view and List view with seamless layout toggle.
  - Filter by content type: Articles, Videos, Images, Audio, Documents, Links, Notes.
  - Status filters: All, Favorites, Archive.
  - Sorting: Newest First, Oldest First, Title (A-Z), Recently Opened.
- **Many-to-Many Playlists**:
  - Save items into multiple playlists without storage duplication.
  - Default "Inbox" collection for rapid bookmarking.
  - Custom playlist icons, item count indicators, and search inside collections.
- **Dual Viewing Experience**:
  - In-app web reader with loading indicator for articles and web links.
  - Local and streamable video playback powered by AndroidX Media3 (ExoPlayer).
  - High-definition image viewer with Coil.
  - Safe intent launching for original apps (YouTube, Instagram, Reddit, X, etc.) with web fallback.
- **Trash & Soft Deletion**:
  - Soft-delete to Recently Deleted trash with restore or permanent deletion.
- **Storage & Cache Management**:
  - Local scoped media caching with one-tap cache clear and storage breakdown.
- **Design System**:
  - Warm neutral color palette: Warm White (`#FAF9F5`), Sand (`#F2F0E8`), Charcoal (`#141312`), and Warm Amber Accent (`#D48B22`).
  - Prohibited blue and crimson colors strictly excluded.
  - Dynamic System Theme, Light Mode, and Dark Mode support.

---

### Build & Verification Summary

- **Package ID**: `com.laterbox`
- **Version Name**: `1.0.0`
- **Version Code**: `1`
- **Min SDK**: `24` (Android 7.0)
- **Target SDK**: `35` (Android 15)
- **Unit Tests**: Passed (`ContentTypeTest`, `MetadataExtractorTest`, `ThemePaletteTest`)
- **Android Lint**: Passed (`lintDebug`)
- **Release Build**: Passed (`assembleRelease`)

### Release Assets & Checksums

| File | Build Variant | Size | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| `app-release.apk` | Release | ~14.66 MB | `22B1AA3CDDAC0D9D557D829F490070F56D458000CA5CD9F725537F9C59DC2DA3` |
| `app-debug.apk` | Debug | ~21.30 MB | `AE05079B5CFC1406C1830DF6D162EF8B46C447CAFD987BCD59E9AAB6ECAC1CA3` |
