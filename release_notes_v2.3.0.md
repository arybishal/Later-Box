# Later Box v2.3.0

**Phase 1: Rich Notes Editor & Document System**

Later Box v2.3.0 transforms note-taking and memory documentation into a native, full-featured Rich Notes Editor. Users can compose beautifully formatted text, insert inline images between paragraphs, structure documents with headers and multi-style lists, toggle interactive checklists, embed hyperlinks, navigate history with multi-level undo/redo, and enjoy reliable background autosaving—all preserved in a distraction-free, soft minimal design.

---

### Key Capabilities in v2.3.0

#### 1. Structured Document Architecture
* **Native Document Representation:** Documents are structured as sequences of typed blocks (`RichBlock.Text` and `RichBlock.Image`) with inline formatting spans (`RichSpan`).
* **Backward Compatible & Portable:** Existing plain-text notes automatically parse into structured document blocks with zero loss. Document state serializes portably to JSON within Room database entities.
* **Full Backup & Restore Support:** Fully integrated with `BackupManager`. Note structures, formatting spans, and inline image files are automatically archived to and restored from zip backups with path resolution across devices.

#### 2. Rich Text Styling & Typographic Hierarchy
* **Typography:** Document Title, Heading 1 (`H1`), Heading 2 (`H2`), and Normal text styles.
* **Inline Span Formatting:** Bold, Italic, Underline, and Strikethrough with support for mixed formatting across any character selection or range.
* **Paragraph Alignment:** Left, Center, and Right text alignments.
* **Live Visual Feedback:** Live inline visual transformation renders span styling in real-time within Jetpack Compose text fields without external web views.

#### 3. Lists & Interactive Checklists
* **Bulleted Lists:** Clean bulleted items with hanging indentation.
* **Numbered Lists:** Auto-incrementing numbering (`1.`, `2.`, `3.`) across sequential blocks.
* **Interactive Checklists:** Tap to check or uncheck tasks with immediate visual strike-through and dimming in both Editing and Reading modes.
* **Intuitive Keyboard Flow:** Pressing Enter automatically continues the current list format. Pressing Enter on an empty list item gracefully exits the list into normal text. Pressing Backspace on an empty item converts or merges blocks seamlessly.

#### 4. Inline Images & Hyperlinks
* **Inline Image Blocks:** Insert photos directly between paragraphs using the native Android Photo Picker.
* **Local Storage & Privacy:** Images are copied to internal app storage via `MediaStorageManager` with portable relative paths.
* **Image Captions & Removal:** Editable image captions and single-tap block deletion.
* **Hyperlink Dialog:** Insert, edit, or remove hyperlinks with custom display text and validation. Clickable in reading mode to open directly in the browser.

#### 5. History & Autosave Engine
* **Undo & Redo:** Multi-level history stack (up to 50 states) with debounced typing snapshot grouping.
* **Debounced Autosave:** Automatically flushes changes to Room database 750ms after user stops typing.
* **Live Status Indicator:** Real-time badge in top bar: `Saving…`, `Saved`, `Local save`, and `Save failed (Retry)`.
* **Immediate Flush:** Guaranteed synchronous persistence on screen exit, app backgrounding, or back navigation.

#### 6. Distraction-Free Reading Mode
* **Reader View:** Dedicated clean reading layout with formatted typography, clickable hyperlinks, and hidden toolbars.
* **Interactive Reading Checklists:** Check or uncheck tasks directly while reading without needing to enter editing mode.
* **Seamless Mode Toggling:** One-tap toggle via top bar icon or floating action button (FAB).

---

### Verification & Quality Assurance

* **Unit Test Suite:** 109 passing unit tests (`.\gradlew.bat testDebugUnitTest`), including comprehensive document serialization, plain-text migration, list continuation, span adjustments, and checklist state preservation.
* **Android Lint:** 0 errors (`.\gradlew.bat lintDebug`).
* **Physical Device Testing:** Verified on OnePlus CPH2691 (`3B15B800NRT00000`, Android 15):
  - Created notes with titles, H1 headings, bold, italic, and checklists.
  - Interactive checklist toggle tested in both Editing and Reading modes.
  - Tested soft keyboard insets: formatting toolbar dynamically adjusts above keyboard without obstructing editing area.
  - Verified Light and Dark themes according to approved design tokens.
  - Verified autosave and immediate flush on back navigation to Home screen.

---

### Release Assets & Checksums

| Asset | File | Size | SHA-256 Checksum |
| :--- | :--- | :--- | :--- |
| **Release APK** | `app-release.apk` | 16.2 MB (16,200,699 bytes) | `580AB9A35033B48455FCDCEB563A772CF4CE344403780AAC4D9D2336CD3191B6` |
| **Debug APK** | `app-debug.apk` | 23.7 MB (23,739,024 bytes) | `0C044398315F3685CBB01400AABD78FE04D9BF97A8C00EBC9E87C517B08790E7` |

*Built with Kotlin 2.0, Jetpack Compose, Room 2.6, Coil, Coroutines, StateFlow, and Android SDK 35.*
