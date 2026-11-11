# Later Box Demo Data Lab
## Synthetic Test Data Generation, Benchmark & Stress Testing Utility

### 1. Overview
The **Later Box Demo Data Lab** is a developer and QA utility integrated directly into Later Box. It generates controlled, deterministic, high-fidelity synthetic test datasets to exercise all application layers—Room persistence, full-text search, rich document editing, playlists, filters, precomputed Gemini AI states, reminder triggers, and backup/restore workflows—without manual data entry or external API calls.

---

### 2. Accessing the Demo Data Lab
The Demo Data Lab is available via:
- **Settings → Developer Tools → Demo Data Lab**

#### Visibility Controls:
1. **Debug Builds (`BuildConfig.DEBUG`):** Always visible in the Settings screen below the In-App Updates section.
2. **Release / Pre-release Builds:** Hidden by default to preserve a clean consumer experience. Unlocked by tapping the **Version** number 7 consecutive times under **Settings → About**. A confirmation snackbar will announce: *"Developer Mode Enabled! Demo Data Lab unlocked."*

---

### 3. Exact Dataset Distributions

#### A. Standard Combined Dataset (Exactly 200 Records)
Tapping **Generate Complete Dataset (200 Records)** generates:
- **100 General Memories** (`demoRecordType = "GENERAL"`)
- **100 Rich Notes** (`demoRecordType = "RICH_NOTE"`)

#### B. General Memories Dataset (100 Records Breakdown)
| Category | Count | Content Type | Key Features |
|---|---|---|---|
| **Saved Webpages & Articles** | 20 | `ARTICLE`, `LINK` | High-fidelity tech, architecture, wellbeing, and travel articles with read progress and domains |
| **Images & Screenshots** | 15 | `IMAGE` | Local JPEG fixtures generated via Canvas across 4 aspect ratios (16:9, 9:16, 4:3, 1:1), Snaps, and camera/gallery source tagging |
| **Quick Text Captures** | 15 | `NOTE` | Snaps with `captureMethod = "QUICK_NOTE"`, quotes, code snippets, packing lists |
| **Voice / Audio Memos** | 10 | `AUDIO` | Pre-populated speech-to-text transcripts, voice recorder source attribution, and audio waveforms |
| **Tasks & Reminders** | 10 | `NOTE` | Due dates spanning overdue (-2d), today (0d), upcoming (+1d to +30d), and completed tasks |
| **Smart Birthdays & Anniversaries** | 10 | `NOTE` | Turning age calculations, milestone silver anniversaries, leap-day birthdays, and multi-offset notification alarms |
| **Videos & Media References** | 5 | `VIDEO` | Technical talks, masterclasses, and lectures from YouTube, Vimeo, TED with read progress |
| **Research & Whitepapers** | 5 | `DOCUMENT` | ArXiv, IEEE, and ACM paper PDFs covering local-first databases, LLM quantization, and ergonomics |
| **Mixed Social & Community** | 10 | `LINK` | Tech threads and posts from Twitter/X, Reddit, LinkedIn, Mastodon, and GitHub |

#### C. Rich Notes Dataset (100 Notes Breakdown)
Exercising the native `RichDocument` JSON format, span parsing, list nesting, and inline media:
| Note Type Category | Count | Structural Characteristics |
|---|---|---|
| **Plain Formatted** | 10 | Unformatted clean text with paragraph breaks |
| **Heading-Rich** | 10 | Strict H1 (`HEADING_1`), H2 (`HEADING_2`), H3 (`HEADING_3`) hierarchy |
| **Mixed Spans** | 10 | Dense combinations of **bold**, *italic*, <u>underline</u>, and ~strikethrough~ spans |
| **Bulleted Lists** | 10 | Multi-item bullet points (`ListType.BULLET`) |
| **Numbered Lists** | 10 | Sequential ordered lists (`ListType.NUMBERED`) |
| **Interactive Checklists** | 15 | Native task lists (`ListType.CHECKLIST`) with verified mixed checked/unchecked states |
| **Image-Rich** | 15 | Single inline image blocks (`RichBlock.Image`) referencing local generated media fixtures with captions |
| **Multiple Images** | 5 | Multi-image documents with interleaved text and photo layouts |
| **Hyperlink-Rich** | 5 | Documents with valid inline clickable `SpanType.LINK` spans |
| **Long-Form** | 5 | Multi-page structured essays (5+ paragraphs, quotes, mixed blocks) |
| **Complex Mixed** | 5 | Comprehensive native documents exercising headings, images, links, checklists, quotes, and body text in a single note |

#### D. Stress Testing Options
Available directly in the UI for performance benchmarking:
- **+500 Stress Items**
- **+1,000 Stress Items**
- **+5,000 Stress Items**

---

### 4. Deterministic Seed & Precomputed AI Fixtures
- **Deterministic Generation:** Uses seed `42` by default. Can be toggled in the UI for reproducible testing across devices.
- **Precomputed Gemini AI Fixtures (30 Items):** Precomputed synthetic AI analysis objects (`AiAnalysisEntity`) are inserted with categories, summaries, key takeaways, and detected entities. This allows immediate testing of AI insight chips, search filtering, and smart grouping **without requiring an active network connection or consuming Gemini API quota**.

---

### 5. Provenance & Absolute Data Safety
Every synthetic record is strictly isolated:
1. **Schema Column:** `isDemo: Boolean` (indexed in Room).
2. **Type Tag:** `demoRecordType: String?` (`"GENERAL"`, `"RICH_NOTE"`, `"STRESS"`).
3. **Safe Deletion Policy:**
   - Removal queries filter strictly by `WHERE isDemo = 1`.
   - Real user items (`isDemo = 0`) are **never touched, modified, or deleted**.
   - Demo playlists are only pruned if they contain **zero user items**.
   - Generated synthetic media files (`demo_img_*.jpg`) are deleted from app storage only if unreferenced by user items.
4. **Backup / Restore:** Demo provenance flags are preserved during JSON export and restored faithfully in both `REPLACE` and `MERGE` modes.

---

### 6. Room Schema Architecture
- **Database Version:** Bumped from `5` to `6`.
- **Migration:** `MIGRATION_5_6` safely adds `isDemo` and `demoRecordType` columns with indices to `saved_items` and `isDemo` to `playlists`.

---

### 7. Automated Test Suite
Run the dedicated test suite:
```powershell
.\gradlew.bat testDebugUnitTest --tests "com.laterbox.data.demo.DemoDataLabTest"
```
Tests verify:
- Exact counts for General (100), Rich Notes (100), and Combined (200).
- Category distributions and checklist state preservation.
- Rich document JSON serialization and round-trip deserialization.
- Image fixture specs and ARGB rendering.
- Precomputed AI analysis fixture integrity (30 items).
- Absolute separation of user vs demo provenance flags.
