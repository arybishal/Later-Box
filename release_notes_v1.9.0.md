# Later Box v1.9.0 — Global Design System Update & Update Checker Repair

Later Box v1.9.0 introduces the official **Minimal. Premium. Monochrome. Clean.** design system and completes a comprehensive repair of the in-app update checker.

---

### What's New in v1.9.0

#### 1. Official Monochrome Design System
- **Yellow Permanently Removed**: Completely eradicated yellow, amber, ochre, gold, and all decorative accent colors from the application interface.
- **Light Theme Standards**: Pure White canvas (`#FFFFFF`), solid black typography and icons (`#111111`), secondary neutral grey typography (`#666666`), subtle borders (`#E5E5E5`), and neutral grey secondary surfaces (`#F2F2F2`).
- **Dark Theme Standards**: Deep black canvas (`#000000`), primary surface (`#171717`), secondary surface (`#222222`), clean white typography and icons (`#FFFFFF`), secondary grey typography (`#B3B3B3`), and borders (`#303030`).
- **Standardized Manage Button**:
  - Light theme: Solid black container (`#111111`) with white text and chevron (`#FFFFFF`).
  - Dark theme: Clean white container (`#FFFFFF`) with dark text and chevron (`#111111`).
  - Minimum 48dp touch target, 10dp rounded corners, and distinct pressed/disabled states.
- **Strictly Slate Destructive**: Deletion and destructive actions use neutral slate (`#374151` / `#4B5563`), adhering strictly to the zero-red-font standard.

#### 2. Repaired In-App Update Checker
- **Root Cause Resolved**: Fixed GitHub 404 lookup by querying `/releases/latest` first and automatically falling back to published releases list `/releases?per_page=20`.
- **Accurate Error Reporting**: Added granular exceptions for Offline (`UpdateCheckException.Offline`), Rate Limiting (`RateLimited`), Server Unavailable (`ServerUnavailable`), and Repo Not Found (`RepoNotFound`), eliminating misleading 404 messages.
- **Asset Filtering**: Automatically filters out debug APKs (`app-debug.apk`) and non-APK assets (`.tar.gz`), selecting the official release APK (`app-release.apk`).
- **Checksum Verification**: Extracts SHA-256 digests from GitHub release notes and asset metadata, validating downloaded APKs prior to installation.

#### 3. Complete Application Visual Alignment
- Cleaned and aligned: Home Dashboard, Library Grid & List, Saved Item Detail, Content Viewer, Settings, Gemini AI settings & diagnostics, Backup & Restore, Add/Edit Collection dialogs, Save Directly sheet, and permission notice cards.

---

### Verification & Checksums

| Asset | Size | SHA-256 Checksum |
| :--- | :--- | :--- |
| **`app-release.apk`** | 15,953,887 bytes | `3dd82f07b0db10ce989f50a10473338f67429396f7b15de4e4735b3ad22d1641` |
| **`app-debug.apk`** | 23,295,608 bytes | `73be5a67884972702616710b041757f6bccc90eaa09b1cd045480e53141fd2a8` |
