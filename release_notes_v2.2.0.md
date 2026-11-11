# Later Box v2.2.0

**Product Refinement, Gemini Auto-Intelligence, Smart Events & UI Fixes**

Later Box v2.2.0 brings automatic Gemini AI intelligence to every saved memory, introduces an annual recurrence engine for smart birthdays and anniversaries with turning-age calculations and multi-stage notifications, elevates AI & Intelligence to the top of Settings, repairs the Library Grid/List toggle with 0ms latency, and adds a non-blocking first-launch update checker.

---

### Key Improvements in v2.2.0

#### 1. AI & Intelligence Prioritized at Position #1 in Settings
* Moved **AI & Intelligence** to the very top of Settings directly beneath the screen header.
* Clear visual status badges: **Active**, **Setup Required**, or **Paused**, indicating model name (`gemini-2.5-flash`) and auto-analysis state.
* Clean, streamlined settings hierarchy:
  1. AI & Intelligence
  2. Appearance & Theming
  3. Reminders & Reliability (Exact alarms, battery optimization, notifications)
  4. Saving & Organizing
  5. Storage & Cleanup (Media cache & recently deleted)
  6. Data & Backup (Full export/import)
  7. Privacy & Local Storage
  8. In-App Updates & About

#### 2. Automatic Gemini Intelligence for All Content Types
* **Zero-tap AI Enrichment:** Every newly saved memory—shared web links, social media links, manual notes, snap captures, camera photos, voice recordings, and documents—now automatically enqueues background Gemini analysis upon save.
* Automatically derives intelligent titles, concise executive summaries, 2–4 key takeaways, topic category tags, and extracted people/places/entities without requiring manual item-by-item analysis triggers.
* Multimodal support analyzes captured images and excerpts directly.
* Enforces user data preferences, including optional Wi-Fi only constraints and transient failure retry policies.

#### 3. Bulk Analysis & AI Privacy Notice
* Added an **Analyze Existing Memories** card in Gemini settings with real-time linear progress tracking, item count (`Analyzing X of Y...`), and cancellation support.
* Prominent **AI Accuracy & Privacy Notice** clarifying that Gemini outputs are automated suggestions, and highlighting Later Box's 100% local-first data architecture.

#### 4. Smart Events (Birthdays & Anniversaries) Engine
* **Dedicated Creation Forms:** Distinct, tailored dialog workflows for Birthdays (Person Name, Relationship chips, Birth Date with Year, Approaching alerts) and Anniversaries (Partner 1 & Partner 2, Anniversary Date with Year, Approaching alerts).
* **Permanent Date Storage:** Real birth and anniversary years are permanently stored in Room database.
* **Annual Recurrence Engine:** Dynamic recurrence calculator handles year rollovers, leap years, and Gregorian February 29 policy (maps to February 28 in non-leap years).
* **Turning Age & Countdown:** Displays turning age ("Turning 31", "Celebrating 9 years") and contextual countdown pills ("Today! 🎉", "Tomorrow", "In 12 days").

#### 5. Multiple Approaching Notifications Schedule
* Configurable reminder schedules per event: **30 days**, **7 days**, **3 days**, **1 day**, and **on the day (0 days)** before.
* Independent, collision-free alarm requestCodes `(id * 100 + offsetDays)` ensure multiple alerts can be scheduled simultaneously without overwriting one another.
* Contextual notification copy: *"Alex's birthday is in 7 days (Turning 31)"*, *"Today is Alex's birthday!"*.
* Reschedules all active approaching alerts across system reboots, time changes, and timezone changes.

#### 6. Instant Library Grid/List ViewMode Toggle
* Fixed latency by backing `viewMode` with an in-memory `_viewMode = MutableStateFlow` in `LibraryViewModel`, giving instant 0ms UI recomposition while persisting asynchronously to DataStore.
* In List mode, renders compact horizontal `ContentCardList` rows (80dp thumbnail, title, domain, favorite, menu) rather than tall vertical feed cards.

#### 7. Non-Blocking First-Launch Update Check
* Performs a silent, non-blocking GitHub release check upon first app launch.
* Prompts the update details dialog only when a newer version is available; remains completely silent when current.
* Preserves retry eligibility if the device was offline during the initial check.

#### 8. Database Migration & Reliability
* **Safe Migration 4 -> 5:** Added `originalDate`, `relationship`, `secondPersonName`, and `reminderOffsetsDaysJson` to `reminders` table without destructive fallback.
* **Zero Accessibility/Gesture Code:** Verified complete removal of cross-app accessibility services.
* **Zero Lint Errors & Full Unit Test Suite:** All 27 unit tests passing.

---

### Release Assets & Verification

| Asset | File | Size | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| **Release APK** | `app-release.apk` | 16.1 MB (16,118,779 bytes) | `541EF72D6281FF6DEBE96764DE756BAF8A8B167F6FDA6865908F6F514FC3B152` |

*Built and verified with Kotlin 2.0, Jetpack Compose, Room 2.6, and Android SDK 35.*
