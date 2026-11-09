# Later Box — Design System & Visual Specification

This document details the official design system, exact color palette, typography hierarchy, component blueprints, and visual principles for the **Later Box** Android application, sampled and calibrated directly from the reference interface screenshots.

---

## 1. Exact Color Palette

The Later Box aesthetic embodies a warm, editorial, tactile paper-and-ink sensibility.
**Strict Constraint**: Zero blue, zero bluish leaning colors, and zero crimson tones.

### 1.1 Light Mode (Reference Screenshot Direct Calibration)

| Token Name | Hex Code | RGB | Usage & Screenshot Element |
| :--- | :--- | :--- | :--- |
| **`WarmCreamBackground`** | `#F4F0E8` | `(244, 240, 232)` | Main screen backdrop across Home, Library, Search, and Settings (51.3% of pixels). |
| **`WarmCardBackground`** | `#FBF8F3` | `(251, 248, 243)` | Card surfaces, empty state dashed cards, and bottom navigation bar (35.0% of pixels). |
| **`WarmCaramelAccent`** | `#9C6E1E` | `(156, 110, 30)` | Primary action buttons (`Save something`, `New`) and center circular `Save (+)` tab button (2.65% of pixels). |
| **`WarmCaramelAccentLight`**| `#AE7C24` | `(174, 124, 36)` | Interaction hover, press states, and badge highlights. |
| **`WarmCaramelSubtle`** | `#FDF6EA` | `(253, 246, 234)` | Subtle warm tint for containers and active selection indicators. |
| **`WarmTextPrimaryLight`** | `#1C1915` | `(28, 25, 21)` | High-contrast editorial titles (`Good evening`, `Library`), button labels, and active navigation tab icons. |
| **`WarmTextSecondaryLight`**| `#5C564E` | `(92, 86, 78)` | Subtitles (`Save now. Enjoy later.`, `0 items`), inactive navigation icons, and sort labels. |
| **`WarmBorderDashed`** | `#E2D9CC` | `(226, 217, 204)` | Dashed border surrounding empty state cards (`cornerRadius: 16.dp`). |
| **`WarmBorderLight`** | `#E2D9CC` | `(226, 217, 204)` | Subtle dividers, search bar stroke, and top border line above bottom navigation bar. |
| **`PillInactiveBackground`**| `#EFE8DC` | `(239, 232, 220)` | Inactive category filter pills (`Inbox`, `Videos`, `Articles`, etc.). |
| **`PillActiveBackground`** | `#1C1915` | `(28, 25, 21)` | Active category filter pill (`All`) with white text. |
| **`GridListActiveBackground`**| `#F3E4C4` | `(243, 228, 196)` | Active pill indicator inside the `[ Grid | List ]` toggle control. |
| **`ScrollThumbColor`** | `#716C64` | `(113, 108, 100)` | Horizontal scroll track indicator thumb below filter pills. |

### 1.2 Dark Mode (Adaptive Warm Charcoal Palette)

| Token Name | Hex Code | RGB | Usage |
| :--- | :--- | :--- | :--- |
| **`CharcoalBackgroundDark`**| `#161412` | `(22, 20, 18)` | Deep charcoal main canvas with warm neutral undertone. |
| **`CharcoalSurfaceDark`** | `#1E1B18` | `(30, 27, 24)` | Card surfaces, modals, and bottom bar. |
| **`CharcoalSurfaceVariant`**| `#282522` | `(40, 37, 34)` | Inactive pills and input fill. |
| **`WarmTextPrimaryDark`** | `#F4F0E8` | `(244, 240, 232)` | Headings and primary text in dark mode. |
| **`WarmTextSecondaryDark`** | `#9E978D` | `(158, 151, 141)` | Secondary body and metadata text. |
| **`CharcoalBorderDark`** | `#2C2723` | `(44, 39, 35)` | Borders and dividers. |
| **`WarmCaramelAccentDark`** | `#B38029` | `(179, 128, 41)` | Buttons and badges in dark mode. |

### 1.3 Destructive & Warning

| Token Name | Hex Code | Usage |
| :--- | :--- | :--- |
| **`WarmTerracotta`** | `#9E3E2F` | Earthy brick red for deletion and destructive confirmations (strictly non-crimson). |
| **`WarmTerracottaDark`** | `#CF5E4D` | Dark mode destructive accent. |

---

## 2. Typography System

The Later Box typography balances **literary elegance** with **functional modern legibility**.

### 2.1 Font Families
- **Display & Headings**: `FontFamily.Serif` (Android high-contrast serif typeface).
  - Used for screen titles (`Good evening`, `Library`, `Search`, `Your playlists`, `Recent saves`).
  - Used for empty state titles (`Nothing saved yet`, `Your library is empty`, `Search everything you saved`).
- **Body & Microcopy**: `FontFamily.Default` (Clean neutral Sans-Serif).
  - Used for metadata, subtitles, filter labels, buttons, input fields, and navigation labels.

### 2.2 Typography Scale

| Style | Font Family | Weight | Size | Line Height | Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Display Large** | Serif | Bold | `34.sp` | `40.sp` | Top level greetings & primary heroes |
| **Display Medium**| Serif | Bold | `32.sp` | `38.sp` | Screen headers (`Library`, `Search`) |
| **Headline Large** | Serif | Bold | `28.sp` | `34.sp` | Major section headers |
| **Headline Medium**| Serif | Bold | `24.sp` | `30.sp` | Dashed card titles & modal titles |
| **Headline Small** | Serif | SemiBold| `20.sp` | `26.sp` | Playlist card names & content card titles |
| **Title Medium** | Sans | SemiBold| `15.sp` | `20.sp` | Card titles in compact lists |
| **Body Large** | Sans | Normal | `15.sp` | `22.sp` | Article excerpts & reader content |
| **Body Medium** | Sans | Normal | `14.sp` | `20.sp` | Descriptions, empty state subtitles |
| **Body Small** | Sans | Normal | `12.sp` | `16.sp` | URLs, domains, item dates |
| **Label Large** | Sans | Bold | `13.sp` | `16.sp` | Buttons (`Save something`, `New`) |
| **Label Small** | Sans | Medium | `11.sp` | `14.sp` | Bottom navigation bar text labels |

---

## 3. Component Architecture

### 3.1 5-Destination Bottom Navigation Bar
- **Container**: `WarmCardBackground` (`#FBF8F3`) with `1.dp` solid top border in `WarmBorderLight` (`#E2D9CC`).
- **Navigation Tabs**:
  1. **Home**: Outlined House icon + `"Home"`.
  2. **Library**: Custom 4-book spine icon (`|||\`) + `"Library"`.
  3. **Save (+)**: Elevated circular badge (`40.dp`) with `WarmCaramelAccent` (`#9C6E1E`) background, dark plus icon (`#1C1915`), and `"Save"` label below.
  4. **Search**: Outlined Search icon + `"Search"`.
  5. **Settings**: Outlined Settings gear icon + `"Settings"`.
- **States**: Active tab tint is `#1C1915` (Light) / `#F4F0E8` (Dark); inactive is `#5C564E` (Light) / `#9E978D` (Dark).

### 3.2 Dashed Empty State Card (`EmptyDashedCard`)
- **Dimensions**: Full width, `16.dp` corner radius, `24.dp` horizontal padding, `36.dp` vertical padding.
- **Border**: Custom stroke (`3px`), dashed effect (`16px` dash, `12px` gap) in `#E2D9CC`.
- **Background**: `#FBF8F3`.
- **Content Alignment**: Vertically and horizontally centered.
- **Button**: Rounded rectangle (`12.dp` radius), `#9C6E1E` background with `#1C1915` bold text.

### 3.3 View Toggle Pill (`GridListTogglePill`)
- **Container**: Rounded capsule (`12.dp`), `#FBF8F3` background, `#E2D9CC` border.
- **Active Pill**: `#F3E4C4` background with `#1C1915` bold text.
- **Inactive Pill**: Transparent background with `#5C564E` normal text.

### 3.4 Filter Pills Row (`PillFilterRow`)
- **Items**: `All`, `Inbox`, `Videos`, `Articles`, `Images`, `Documents`, `Audio`, `Notes`.
- **Active Pill**: `#1C1915` background with `#FFFFFF` bold text.
- **Inactive Pill**: `#EFE8DC` background with `#1C1915` medium text.
- **Scroll Track**: Located beneath chips with left arrow (`◀`), centered track, thumb indicator (`#716C64`), and right arrow (`▶`).

### 3.5 Sort Selector (`SortDropdownSelector`)
- Compact dropdown with `"Sort"` label in `#5C564E`, followed by a rounded pill showing current sort order (`Newest first`) and a chevron dropdown indicator.
