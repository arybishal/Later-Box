# Later Box — Design System & Visual Specification

This document details the official design system, exact color palette, typography hierarchy, component blueprints, and visual principles for the **Later Box** Android application, sampled and calibrated directly from the reference interface screenshots.

---

## 1. Exact Color Palette

The Later Box aesthetic embodies a clean, modern, high-contrast visual system:
**Core Scheme**: Pure White Background (`#FFFFFF`), Solid Black Text (`#000000`), and Vibrant Yellow Accent (`#F5BA13`).
**Strict Constraints**: Strictly ZERO brown/ochre (`#9E6F21` or `#9C6E1E`), zero red fonts, and zero crimson tones.

### 1.1 Light Mode Specification

| Token Name | Hex Code | RGB | Usage & Screenshot Element |
| :--- | :--- | :--- | :--- |
| **`PureWhiteBackground`** | `#FFFFFF` | `(255, 255, 255)` | Main screen backdrop across Home, Library, Search, and Settings. |
| **`CleanCardBackground`** | `#FFFFFF` | `(255, 255, 255)` | Card surfaces, empty state dashed cards, and bottom navigation bar. |
| **`VibrantYellowAccent`** | `#F5BA13` | `(245, 186, 19)` | Primary action buttons (`Save something`, `New`) and center circular `Save (+)` tab button. |
| **`VibrantYellowAccentLight`**| `#FDE047` | `(253, 224, 71)` | Interaction hover, press states, and badge highlights. |
| **`VibrantYellowSubtle`** | `#FEF9C3` | `(254, 249, 195)` | Soft yellow tint for containers and active selection indicators. |
| **`SolidBlackText`** | `#000000` | `(0, 0, 0)` | High-contrast titles (`Good afternoon`, `Library`), button labels, and active navigation tab icons. |
| **`CharcoalTextSecondary`**| `#4B5563` | `(75, 85, 99)` | Subtitles (`Save now. Enjoy later.`, `0 items`), inactive navigation icons, and sort labels. |
| **`SubtleBorderLight`** | `#E5E7EB` | `(229, 231, 235)` | Dashed border surrounding empty state cards, dividers, and input borders. |
| **`PillInactiveBackground`**| `#F3F4F6` | `(243, 244, 246)` | Inactive category filter pills (`Inbox`, `Videos`, `Articles`, etc.). |
| **`PillActiveBackground`** | `#000000` | `(0, 0, 0)` | Active category filter pill (`All`) with white text. |
| **`GridListActiveBackground`**| `#FEF08A` | `(254, 240, 138)` | Active pill indicator inside the `[ Grid \| List ]` toggle control. |
| **`ScrollThumbColor`** | `#4B5563` | `(75, 85, 99)` | Horizontal scroll track indicator thumb below filter pills. |

### 1.2 Dark Mode (Adaptive Warm Charcoal Palette)

| Token Name | Hex Code | RGB | Usage |
| :--- | :--- | :--- | :--- |
| **`CharcoalBackgroundDark`**| `#161412` | `(22, 20, 18)` | Deep charcoal main canvas with warm neutral undertone. |
| **`CharcoalSurfaceDark`** | `#1E1B18` | `(30, 27, 24)` | Card surfaces, modals, and bottom bar. |
| **`CharcoalSurfaceVariant`**| `#282522` | `(40, 37, 34)` | Inactive pills and input fill. |
| **`WarmTextPrimaryDark`** | `#F4F0E8` | `(244, 240, 232)` | Headings and primary text in dark mode. |
| **`WarmTextSecondaryDark`** | `#9E978D` | `(158, 151, 141)` | Secondary body and metadata text. |
| **`CharcoalBorderDark`** | `#2C2723` | `(44, 39, 35)` | Borders and dividers. |
| **`VibrantYellowAccentDark`**| `#F5BA13` | `(245, 186, 19)` | Buttons and badges in dark mode. |

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
  3. **Save (+)**: Elevated circular badge (`40.dp`) with `VibrantYellowAccent` (`#F5BA13`) background, dark plus icon (`#000000`), and `"Save"` label below.
  4. **Search**: Outlined Search icon + `"Search"`.
  5. **Settings**: Outlined Settings gear icon + `"Settings"`.
- **States**: Active tab tint is `#000000` (Light) / `#FFFFFF` (Dark); inactive is `#4B5563` (Light) / `#9E978D` (Dark).

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
