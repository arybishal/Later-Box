# Later Box — Design System & Visual Specification

This document details the official design system, exact color palette, typography hierarchy, component blueprints, and visual principles for the **Later Box** Android application, sampled and calibrated directly from the reference interface screenshots.

---

## 1. Exact Color Palette

The Later Box aesthetic embodies a clean, minimalist monochrome visual system:
**Core Scheme**: Pure White Background (`#FFFFFF`) and Solid Black (`#000000`).
**Strict Constraints**: Strictly ZERO yellow, ZERO amber, ZERO brown/ochre (`#9E6F21` or `#9C6E1E`), zero red fonts, zero crimson, and zero multicolors.

### 1.1 Light Mode Specification

| Token Name | Hex Code | RGB | Usage & Element |
| :--- | :--- | :--- | :--- |
| **`PureWhiteBackground`** | `#FFFFFF` | `(255, 255, 255)` | Main screen backdrop across Home, Library, Search, and Settings. |
| **`CleanCardBackground`** | `#FFFFFF` | `(255, 255, 255)` | Card surfaces, empty state dashed cards, and bottom navigation bar. |
| **`PrimaryBlackText`** | `#000000` | `(0, 0, 0)` | High-contrast titles (`Your saved world.`, `Later Box`), headings, active navigation icons, and center `Save (+)` tab. |
| **`NeutralGreySecondary`** | `#555555` | `(85, 85, 85)` | Subtitles (`Everything worth keeping...`), inactive navigation icons, and timestamps. |
| **`MutedTextTertiary`** | `#888888` | `(136, 136, 136)` | Input placeholders, dividers, and subtle metadata. |
| **`SubtleBorderLight`** | `#E5E7EB` | `(229, 231, 235)` | Dashed and solid borders surrounding cards, dividers, and search bar. |
| **`PillInactiveBackground`**| `#F4F4F5` | `(244, 244, 245)` | Inactive category filter pills, badges, and icon containers. |
| **`PillActiveBackground`** | `#000000` | `(0, 0, 0)` | Active category filter pill (`All`) with white text. |
| **`GridListActiveBackground`**| `#000000` | `(0, 0, 0)` | Active pill indicator inside the `[ Grid \| List ]` toggle control with white text. |
| **`ScrollThumbColor`** | `#555555` | `(85, 85, 85)` | Horizontal scroll track indicator thumb below filter pills. |

### 1.2 Dark Mode (Adaptive Monochrome Palette)

| Token Name | Hex Code | RGB | Usage |
| :--- | :--- | :--- | :--- |
| **`CharcoalBackgroundDark`**| `#000000` | `(0, 0, 0)` | Pure black main canvas. |
| **`CharcoalSurfaceDark`** | `#121212` | `(18, 18, 18)` | Card surfaces, modals, and bottom bar. |
| **`CharcoalSurfaceVariant`**| `#1E1E1E` | `(30, 30, 30)` | Inactive pills and input fill. |
| **`WarmTextPrimaryDark`** | `#FFFFFF` | `(255, 255, 255)` | Headings and primary text in dark mode. |
| **`WarmTextSecondaryDark`** | `#AAAAAA` | `(170, 170, 170)` | Secondary body and metadata text. |
| **`CharcoalBorderDark`** | `#2E2E2E` | `(46, 46, 46)` | Borders and dividers. |
| **`WarmCaramelAccentDark`** | `#FFFFFF` | `(255, 255, 255)` | Monochrome white highlight in dark mode. |

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
  3. **Save (+)**: Elevated circular badge (`42.dp`) with solid black (`#000000`) background, white plus icon (`#FFFFFF`), and `"Save"` label below.
  4. **Search**: Outlined Search icon + `"Search"`.
  5. **Settings**: Outlined Settings gear icon + `"Settings"`.
- **States**: Active tab tint is `#000000` (Light) / `#FFFFFF` (Dark) with a delicate neutral grey (`#F4F4F5`) highlight pill behind the active tab icon; inactive is `#555555` (Light) / `#AAAAAA` (Dark).

### 3.2 Redesigned Home Screen Architecture
- **`HomeHeader`**: Premium title "Later Box" in high-contrast Serif, accompanied by a monochrome black-bordered "ARCHIVE" pill tag, and a minimal circular settings entry.
- **`HomeIntroSection`**: Distinctive editorial headline "Your saved world." with supporting microcopy "Everything worth keeping, all in one place."
- **`HomeSearchEntry`**: Full-width rounded search entry with black search icon, neutral background, and keyboard hint pill.
- **`ContinueContentCard`**: Compact (230×100dp) horizontal carousel cards showing media thumbnail, source, 2-line title, and progress bar with high-contrast black indicator.
- **`CollectionShortcutTile`**: Refined compact tiles (120×92dp) for Favorites (highlighted with solid black circle badge and white heart), Inbox, and custom playlists.
- **`RecentSavedItemCard`**: Sleek modern feed card with domain icon, relative time, 2-line title, content type tag in subtle neutral grey (`#F4F4F5`), thumbnail with high-contrast video overlay, and inline favorite/share/playlist actions.
- **`HomeEmptyState`**: Minimalist empty state card with neutral icon badge and high-contrast black action button.

### 3.3 Dashed Empty State Card (`EmptyDashedCard`)
- **Dimensions**: Full width, `16.dp` corner radius, `24.dp` horizontal padding, `36.dp` vertical padding.
- **Border**: Custom stroke (`3px`), dashed effect (`16px` dash, `12px` gap) in `#E5E7EB`.
- **Background**: `#FFFFFF`.
- **Content Alignment**: Vertically and horizontally centered.
- **Button**: Rounded rectangle (`12.dp` radius), `#000000` background with `#FFFFFF` bold text.

### 3.4 View Toggle Pill (`GridListTogglePill`)
- **Container**: Rounded capsule (`12.dp`), `#FFFFFF` background, `#E5E7EB` border.
- **Active Pill**: `#000000` background with `#FFFFFF` bold text.
- **Inactive Pill**: Transparent background with `#555555` normal text.

### 3.5 Filter Pills Row (`PillFilterRow`)
- **Items**: `All`, `Inbox`, `Videos`, `Articles`, `Images`, `Documents`, `Audio`, `Notes`.
- **Active Pill**: `#000000` background with `#FFFFFF` bold text.
- **Inactive Pill**: `#F4F4F5` background with `#000000` medium text.
- **Scroll Track**: Located beneath chips with left arrow (`◀`), centered track, thumb indicator (`#555555`), and right arrow (`▶`).

### 3.6 Sort Selector (`SortDropdownSelector`)
- Compact dropdown with `"Sort"` label in `#555555`, followed by a rounded pill showing current sort order (`Newest first`) and a chevron dropdown indicator.

### 3.7 Saved Item Detail Experience (`ItemDetailScreen`)
- **Canonical Destination**: Single entry point (`detail/{itemId}`) for items opened from Home, Library Grid/List, Search, Playlists, or Favorites.
- **Header**: Compact navigation bar with back arrow, content-type badge pill, reactive favorite heart toggle, and 3-dots overflow menu (Playlists, Notes, Tags, Archive, Share, Copy Link, Trash).
- **Content Preview Centerpiece**:
  - **Images**: High-resolution image in `16.dp` rounded container, preserving aspect ratio, tap to enlarge in fullscreen viewer.
  - **Videos**: Thumbnail container with dark scrim, video badge, and circular play button in soft butter yellow (`#FFF4B8`) with dark icon.
  - **Articles / Links**: Preview card with web image or editorial typography fallback.
  - **Documents**: Polished card with document icon and file metadata.
  - **Notes / Plain Text**: Clean serif/sans editorial note container with preserved typography.
- **Title & Metadata**: High-contrast editorial title (`22.sp`, bold) paired with domain initial badge, domain/source name, relative saved timestamp, and compact URL pill with copy button.
- **About This Item**: Original source description with clean empty fallback when missing, and an extensible architectural slot for Phase 2 AI metadata.
- **Primary Action ("Open in [App]")**: Prominent button in Soft Butter Yellow (`#FFF4B8`) with `#111111` bold text, triggering native social deep linking (Instagram, Facebook, X/Twitter, TikTok, YouTube, Reddit) with browser fallback.
- **Organization**: Reactive inline chips and dedicated dialogs for Playlists (`AddToPlaylistDialog`), Tags (`AddTagDialog`), and Personal Notes (`EditNotesDialog`).
- **Destructive Action**: Clean, slate-colored "Move to Trash" trigger adhering strictly to the zero red font standard.
