# Later Box — Design System & Visual Specification

This document details the official design system, exact color palette, typography hierarchy, component blueprints, and visual principles for the **Later Box** Android application, sampled and calibrated directly from the reference interface screenshots.

---

## 1. Exact Color Palette

The Later Box aesthetic embodies a clean, modern, high-contrast visual system:
**Core Scheme**: Pure White Background (`#FFFFFF`), Primary Black Typography (`#111111`), and Soft Butter Yellow Highlight (`#FFF1A6`).
**Strict Constraints**: Strictly ZERO brown/ochre (`#9E6F21` or `#9C6E1E`), zero red fonts, zero crimson, and zero multicolors.

### 1.1 Light Mode Specification

| Token Name | Hex Code | RGB | Usage & Screenshot Element |
| :--- | :--- | :--- | :--- |
| **`PureWhiteBackground`** | `#FFFFFF` | `(255, 255, 255)` | Main screen backdrop across Home, Library, Search, and Settings. |
| **`CleanCardBackground`** | `#FFFFFF` | `(255, 255, 255)` | Card surfaces, empty state dashed cards, and bottom navigation bar. |
| **`SoftButterYellow`** | `#FFF1A6` | `(255, 241, 166)` | Primary highlight color: `ARCHIVE` tag, `Favorites` shortcut, `Save (+)` tab button, progress bars. |
| **`SoftButterYellowSubtle`**| `#FFFBEA` | `(255, 251, 234)` | Soft yellow tint for containers and badges. |
| **`PrimaryBlackText`** | `#111111` | `(17, 17, 17)` | High-contrast titles (`Your saved world.`, `Later Box`), headings, and active navigation tab icons. |
| **`NeutralGreySecondary`** | `#6B7280` | `(107, 114, 128)` | Subtitles (`Everything worth keeping...`), inactive navigation icons, and timestamps. |
| **`MutedTextTertiary`** | `#9CA3AF` | `(156, 163, 175)` | Input placeholders, dividers, and inactive icons. |
| **`SubtleBorderLight`** | `#E5E7EB` | `(229, 231, 235)` | Dashed and solid borders surrounding cards, dividers, and search bar. |
| **`PillInactiveBackground`**| `#F4F4F5` | `(244, 244, 245)` | Inactive category filter pills and shortcuts. |
| **`PillActiveBackground`** | `#111111` | `(17, 17, 17)` | Active category filter pill (`All`) with white text. |
| **`GridListActiveBackground`**| `#FFF1A6` | `(255, 241, 166)` | Active pill indicator inside the `[ Grid \| List ]` toggle control. |
| **`ScrollThumbColor`** | `#6B7280` | `(107, 114, 128)` | Horizontal scroll track indicator thumb below filter pills. |

### 1.2 Dark Mode (Adaptive Charcoal Palette)

| Token Name | Hex Code | RGB | Usage |
| :--- | :--- | :--- | :--- |
| **`CharcoalBackgroundDark`**| `#0D0D0E` | `(13, 13, 14)` | Deep charcoal canvas with modern dark mode contrast. |
| **`CharcoalSurfaceDark`** | `#17171A` | `(23, 23, 26)` | Card surfaces, modals, and bottom bar. |
| **`CharcoalSurfaceVariant`**| `#222226` | `(34, 34, 38)` | Inactive pills and input fill. |
| **`WarmTextPrimaryDark`** | `#EDEDED` | `(237, 237, 237)` | Headings and primary text in dark mode. |
| **`WarmTextSecondaryDark`** | `#A1A1AA` | `(161, 161, 170)` | Secondary body and metadata text. |
| **`CharcoalBorderDark`** | `#27272A` | `(39, 39, 42)` | Borders and dividers. |
| **`WarmCaramelAccentDark`** | `#FFF1A6` | `(255, 241, 166)` | Subtle butter-yellow highlight in dark mode. |

### 1.3 Destructive & Warning (Zero Red Font Constraint)

| Token Name | Hex Code | Usage |
| :--- | :--- | :--- |
| **`SlateDestructive`** | `#374151` | Neutral slate/charcoal for deletion and destructive confirmations (strictly NO red font). |
| **`SlateDestructiveDark`** | `#4B5563` | Dark mode destructive accent. |

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
- **Container**: `CleanCardBackground` (`#FFFFFF`) with `1.dp` solid top border in `SubtleBorderLight` (`#E5E7EB`).
- **Navigation Tabs**:
  1. **Home**: Outlined House icon + `"Home"`.
  2. **Library**: Custom 4-book spine icon (`|||\`) + `"Library"`.
  3. **Save (+)**: Elevated circular badge (`42.dp`) with `SoftButterYellow` (`#FFF1A6`) background, dark plus icon (`#111111`), and `"Save"` label below.
  4. **Search**: Outlined Search icon + `"Search"`.
  5. **Settings**: Outlined Settings gear icon + `"Settings"`.
- **States**: Active tab tint is `#111111` (Light) / `#EDEDED` (Dark) with a delicate `SoftButterYellow` (`#FFF1A6`) highlight pill behind the active tab icon; inactive is `#6B7280` (Light) / `#A1A1AA` (Dark).

### 3.2 Redesigned Home Screen Architecture
- **`HomeHeader`**: Premium title "Later Box" in high-contrast Serif, accompanied by a soft butter-yellow "ARCHIVE" pill tag, and a minimal circular settings entry.
- **`HomeIntroSection`**: Distinctive editorial headline "Your saved world." with supporting microcopy "Everything worth keeping, all in one place."
- **`HomeSearchEntry`**: Full-width rounded search entry with black search icon, neutral background, and keyboard hint pill.
- **`ContinueContentCard`**: Compact (230×100dp) horizontal carousel cards showing media thumbnail, source, 2-line title, and progress bar with soft yellow indicator.
- **`CollectionShortcutTile`**: Refined compact tiles (120×92dp) for Favorites (highlighted with soft yellow badge), Inbox, and custom playlists.
- **`RecentSavedItemCard`**: Sleek modern feed card with domain icon, relative time, 2-line title, content type tag in soft butter-yellow, thumbnail with video overlay, and inline favorite/share/playlist actions.
- **`HomeEmptyState`**: Minimalist empty state card with soft yellow icon badge and high-contrast black action button.
- **`HomeQuickSaveFAB`**: Floating action button (54dp) in solid black with soft butter-yellow plus icon.

### 3.2 Dashed Empty State Card (`EmptyDashedCard`)
- **Dimensions**: Full width, `16.dp` corner radius, `24.dp` horizontal padding, `36.dp` vertical padding.
- **Border**: Custom stroke (`3px`), dashed effect (`16px` dash, `12px` gap) in `#E5E7EB`.
- **Background**: `#FFFFFF`.
- **Content Alignment**: Vertically and horizontally centered.
- **Button**: Rounded rectangle (`12.dp` radius), `#F5BA13` background with `#000000` bold text.

### 3.3 View Toggle Pill (`GridListTogglePill`)
- **Container**: Rounded capsule (`12.dp`), `#FFFFFF` background, `#E5E7EB` border.
- **Active Pill**: `#FEF08A` background with `#000000` bold text.
- **Inactive Pill**: Transparent background with `#4B5563` normal text.

### 3.4 Filter Pills Row (`PillFilterRow`)
- **Items**: `All`, `Inbox`, `Videos`, `Articles`, `Images`, `Documents`, `Audio`, `Notes`.
- **Active Pill**: `#000000` background with `#FFFFFF` bold text.
- **Inactive Pill**: `#F3F4F6` background with `#000000` medium text.
- **Scroll Track**: Located beneath chips with left arrow (`◀`), centered track, thumb indicator (`#4B5563`), and right arrow (`▶`).

### 3.5 Sort Selector (`SortDropdownSelector`)
- Compact dropdown with `"Sort"` label in `#5C564E`, followed by a rounded pill showing current sort order (`Newest first`) and a chevron dropdown indicator.
