# Later Box v1.7.2 — Minimal Dropdown Selector for Gemini Models

## Overview
Later Box **v1.7.2** transforms the Gemini API model selection experience with a sleek, minimal dropdown selector. Rather than displaying an extensive list of 40+ discovered models across the entire settings screen, v1.7.2 introduces an expandable card interface that stays compact when collapsed and provides smooth search, family filtering, and token limit details when expanded.

---

## What's New in v1.7.2

### 1. Minimal Model Dropdown Selector
- **Compact Collapsed State:**
  - Displays the currently selected model cleanly in a single, well-proportioned card.
  - Highlights model name, API resource identifier in monospace font, token context limits (e.g. `1M in · 8K out`), and a soft butter-yellow `RECOMMENDED` badge when applicable.
  - Integrated animated expand/collapse chevron arrow (`KeyboardArrowDown` / `KeyboardArrowUp`).
- **Smooth Expandable Selection Panel:**
  - Tapping the card expands a dropdown panel without cluttering or stretching the settings page.
  - Features real-time search filtering by model name or ID.
  - Category filter chips (`All`, `Flash`, `Pro`, `Other`) with live item count badges.
  - Constrained scrollable container (`maxHeight = 280.dp`) with smooth internal scrolling, eliminating touch conflicts.
  - Instant selection: Tapping any model automatically persists the selection in secure DataStore and smoothly collapses the dropdown.

### 2. Streamlined Settings Information Architecture
- Greatly reduced screen height in Settings → AI & Intelligence.
- Re-ordered sections for an effortless workflow: API Key → Status & Quota Banner → Minimal Model Dropdown → 6-Step Connection Diagnostics → Automation Settings.

### 3. Strict Visual Identity Compliance
- Designed in accordance with Later Box's signature visual palette:
  - Clean Background: `#FFFFFF`
  - Deep Black Typography & Borders: `#111111`
  - Soft Butter Yellow Highlight: `#FFF4B8`
  - Subtle Neutral Greys: `#F6F6F6` (chips), `#E5E5E5` (dividers)
- Strictly zero prohibited tones (`#9E6F21`, red fonts, amber/gold gradients).

---

## Checksums & Artifacts

| Artifact | Size | SHA-256 Checksum |
| :--- | :--- | :--- |
| `app-release.apk` | 15.2 MB (15,904,735 B) | `55BC26AB4670B282C680732768EF7872D28F9563E3539189E2E88A1A90461B9C` |
| `app-debug.apk` | 22.1 MB (23,213,688 B) | `532A55C3BADDF152F964F26DC3977CD42365D2ADD0CF392A7226A5AB43E3EBDF` |
