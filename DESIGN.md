# Later Box — Official Design System & Visual Specification (v2.0.0)

This document details the official design system, color tokens, typography scale, component blueprints, and visual principles for the **Later Box** Android application (`com.laterbox`).

The visual identity of Later Box is:

**Soft. Minimal. Premium. Spacious. Modern. Content-focused.**

Guided by the clean, modern aesthetic of spacious cards, soft neutral canvases, refined interactive blue accents, generous corner radii, horizontal collections, compact content feeds, and an elevated floating AI assistant.

---

## 1. Official Color Palette

### 1.1 Official Light Theme

| Design Token | Hex Code | RGB | Usage & Specification |
| :--- | :--- | :--- | :--- |
| **Main Background** | `#F0F1F3` | `(240, 241, 243)` | Soft grey main canvas across Home, Library, Search, Playlists, Detail, and Settings |
| **Primary Card Background** | `#FFFFFF` | `(255, 255, 255)` | Pure white cards for saved content, collections, sections, dialogs, and elevated sheets |
| **Secondary Surface** | `#F5F5F5` | `(245, 245, 245)` | Inactive pills, chips, secondary action backgrounds, and subtle card insets |
| **Elevated Surface** | `#FFFFFF` | `(255, 255, 255)` | Floating action pills, modal sheets, and high-elevation dialogs |
| **Primary Text** | `#171717` | `(23, 23, 23)` | High-contrast headlines, titles, active tabs, and primary labels |
| **Secondary Text** | `#777777` | `(119, 119, 119)` | Subtitles, descriptions, domain tags, timestamps, and secondary metadata |
| **Tertiary Text** | `#999999` | `(153, 153, 153)` | Subtle hints, counter text, search placeholders, and inactive labels |
| **Primary Icons** | `#171717` | `(23, 23, 23)` | Primary navigation icons, close icons, search icons, and active symbols |
| **Secondary Icons** | `#8B8B8B` | `(139, 139, 139)` | Inactive icons, metadata indicators, chevrons, and secondary actions |
| **Borders & Dividers** | `#E9E9E9` | `(233, 233, 233)` | Subtle card outlines, dividers, and input borders |
| **Primary Interactive Accent**| `#1687D9` | `(22, 135, 217)` | Meaningful interactive accents: AI actions, links, active selectors, and summarize buttons |
| **Pressed Neutral Surface** | `#E7E8EA` | `(231, 232, 234)` | Active ripple / pressed state feedback on cards and buttons |
| **Disabled Content** | `#B5B5B5` | `(181, 181, 181)` | Inactive/disabled buttons, toggles, and unavailable controls |

### 1.2 Official Dark Theme

| Design Token | Hex Code | RGB | Usage & Specification |
| :--- | :--- | :--- | :--- |
| **Main Background** | `#101113` | `(16, 17, 19)` | Deep charcoal/near-black canvas |
| **Primary Card Background** | `#1C1D20` | `(28, 29, 32)` | Elevated dark cards for saved items, collections, dialogs, and sheets |
| **Secondary Surface** | `#252629` | `(37, 38, 41)` | Inactive chips, inner containers, and input field backgrounds |
| **Elevated Surface** | `#292A2E` | `(41, 42, 46)` | Floating AI assistant pill, active dialogs, and elevated action buttons |
| **Primary Text** | `#FFFFFF` | `(255, 255, 255)` | Pure white headlines, titles, and high-contrast typography |
| **Secondary Text** | `#B7B7BB` | `(183, 183, 187)` | Neutral light grey subtitles, descriptions, and supporting metadata |
| **Tertiary Text** | `#88888E` | `(136, 136, 142)` | Subtle counters, hints, and placeholder text |
| **Primary Icons** | `#FFFFFF` | `(255, 255, 255)` | White navigation and active control icons |
| **Secondary Icons** | `#A0A0A5` | `(160, 160, 165)` | Inactive icons, subtle indicators, and chevrons in dark mode |
| **Borders & Dividers** | `#333438` | `(51, 52, 56)` | Subtle dark dividers and card outlines |
| **Interactive Accent** | `#55A9EE` | `(85, 169, 238)` | High-contrast interactive blue accent for dark mode actions and links |
| **Pressed Neutral Surface** | `#303136` | `(48, 49, 54)` | Dark ripple and touch feedback state |
| **Disabled Content** | `#68686D` | `(104, 104, 109)` | Disabled controls in dark mode |

### 1.3 Interactive Accent & Color Rules

1. **Restrained Blue Usage**: The application is predominantly neutral (soft grey, crisp white, deep charcoal). Blue (`#1687D9` / `#55A9EE`) is reserved strictly for meaningful interactive affordances:
   - "Summarize" and AI prompt action buttons.
   - Text hyperlinks and deep-link indicators.
   - Active/selected chips where indicated.
   - Distinctive primary touch targets.
2. **AI Assistant Visual Identity**: The floating "Ask questions" pill uses a clean white (or `#292A2E` in dark mode) surface with soft shadow and a refined multi-gradient sparkle icon.
3. **No Decorative Gradients**: No large decorative gradients, bright backgrounds, or random palette accents.
4. **Prohibition of Dominant Yellow**: Yellow is permanently removed from the primary UI identity.
5. **Content Integrity**: User-saved images, website thumbnails, screenshots, and media previews preserve their natural, un-tinted colors. Content is not tinted with application theme colors.

### 1.4 Destructive Action Constraint

Destructive actions (e.g. Move to Trash, Clear Cache, Delete Playlist) use a neutral slate styling (`#374151` in Light, `#4B5563` in Dark) with high contrast. Aggressive red fonts are prohibited.

---

## 2. Shape, Elevation & Shadows

| Component Type | Corner Radius | Elevation / Shadow | Description |
| :--- | :--- | :--- | :--- |
| **Large Content Cards** | `26.dp` (24–30dp) | Soft shadow (`elevation = 1.dp`–`2.dp`) | Saved item feed cards and detailed content containers |
| **Collection Cards** | `24.dp` (22–26dp) | `elevation = 1.dp`–`2.dp` | Horizontal scrolling collection and playlist cards |
| **Compact Filter Chips** | Fully rounded (`50%` / `CircleShape`) | `0.dp` | Category filters ("All", "Favorites", "Videos", "Articles") |
| **Action Buttons** | `20.dp`–`24.dp` | `elevation = 0.dp`–`2.dp` | Primary, secondary, and dialog action buttons |
| **Floating AI Pill** | Fully rounded (`CircleShape`) | `elevation = 4.dp` (Soft shadow) | Floating "Ask questions" AI assistant action |
| **Input & Search Fields** | `20.dp` (18–22dp) | `0.dp` | Search bars, dialog text inputs, note fields |
| **Dialogs & Bottom Sheets** | `28.dp` top / all | Modal container | Rounded bottom sheets and alert dialogs |

---

## 3. Typography Hierarchy

The typography balances **editorial serif elegance** for titles with **clean modern sans-serif legibility** for content feeds and functional metadata. Full support for Nepali (Devanagari) and Unicode text without unreadable truncation.

| Style | Font Family | Weight | Size | Line Height | Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Display Large** | Serif | Bold | `34.sp` | `40.sp` | Main hero headings |
| **Display Medium** | Serif | Bold | `30.sp` | `36.sp` | Screen headers (`Later Box`, `Library`, `Search`) |
| **Headline Large** | Serif | Bold | `26.sp` | `32.sp` | Major section headers |
| **Headline Medium** | Sans / Serif | SemiBold | `22.sp` | `28.sp` | Collection titles, modal headers |
| **Headline Small** | Sans | SemiBold | `18.sp` | `24.sp` | Content detail titles, card titles |
| **Title Medium** | Sans | SemiBold | `15.sp` | `20.sp` | Saved item card title (2 lines max) |
| **Body Large** | Sans | Normal | `15.sp` | `22.sp` | Article excerpts, reader content |
| **Body Medium** | Sans | Normal | `13.sp`–`14.sp` | `19.sp` | Descriptions, AI summaries, subtitles |
| **Body Small** | Sans | Normal | `12.sp` | `16.sp` | URLs, domains, relative dates, metadata |
| **Label Large** | Sans | SemiBold | `13.sp` | `16.sp` | Action buttons, floating pills |
| **Label Small** | Sans | Medium | `11.sp` | `14.sp` | Bottom navigation bar labels, badge counts |

---

## 4. Key Screen Architecture & Component Blueprints

### 4.1 Home Screen
1. **Header**:
   - Status tag: Contextual uppercase tracking label (e.g. `SAVED MEMORIES`).
   - Title: Prominent `Later Box` editorial headline (`30.sp`).
   - Subtitle: Dynamic live counter (`${totalCount} items saved`).
   - Actions: Circular search button (`40.dp`) and circular overflow/settings button (`40.dp`).
2. **Activity & Welcome Summary**:
   - Compact summary card displaying dynamic saved memory counts, recent activity, and helpful contextual info. No fake statistics.
3. **Collections Section**:
   - Header with `Collections` title and `+ New` button.
   - Horizontal scrolling row of `24.dp` rounded white cards with collection title, live item count, and representative icon/thumbnail. Includes an `All` card with total count.
4. **Memories Feed Section**:
   - Filter chips row above the feed: `All (${count})`, `Favorites (${count})`, and media source filters with live counts and clear neutral active state.
   - Saved content cards: Compact horizontal white cards (`26.dp` radius, soft shadow):
     - **Left**: Item title (2 lines max), short description or AI summary, source icon + relative date row.
     - **Right**: Rounded thumbnail / media preview (`80.dp` x `80.dp`, `16.dp` radius) with neutral placeholder.
5. **Floating AI Assistant**:
   - Pill-shaped elevated action (`Ask questions` + sparkle icon) floating above the content with soft shadow, wired to Gemini AI.

### 4.2 Library & Playlists Screens
- Soft `#F0F1F3` background, white elevated cards (`24.dp`), compact grid/list toggle, responsive sorting selector, and unified search bar.

### 4.3 Item Detail & Content Viewer
- White card containers, high-contrast readable typography, interactive blue accent for AI analysis, notes, tags, and media viewing.

### 4.4 Settings & Maintenance
- Clean grouped cards (`24.dp`), clear toggle switches, intact GitHub release update checker and SHA-256 validation.

---

## 5. Performance & Accessibility Standards
- **Frame Rate Target:** 120Hz / 90Hz high-refresh-rate fluid scrolling.
- **Touch Targets:** Minimum `48.dp` x `48.dp` touch affordances for all interactive buttons and icons.
- **Contrast Ratio:** WCAG 2.1 AA compliant contrast across Light and Dark themes.
- **Image Pipeline:** Asynchronous Coil image loading with disk/memory caching and fallback placeholders.
