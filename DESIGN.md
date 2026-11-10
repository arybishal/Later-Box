# Later Box — Official Monochrome Design System & Visual Specification

This document details the official design system, color tokens, typography scale, component blueprints, and visual principles for the **Later Box** Android application (`com.laterbox`).

The visual identity of Later Box is:

**Minimal. Premium. Monochrome. Clean.**

Yellow, amber, and all chromatic accent colors have been permanently removed from the application's visual identity.

---

## 1. Official Color Palette

### 1.1 Official Light Theme

| Design Element | Hex Code | RGB | Usage & Description |
| :--- | :--- | :--- | :--- |
| **Main Background** | `#FFFFFF` | `(255, 255, 255)` | Pure white canvas across Home, Library, Search, Detail, and Settings |
| **Secondary Surface** | `#F2F2F2` | `(242, 242, 242)` | Inactive pills, tags, search bar fills, elevated surfaces, and metadata containers |
| **Borders & Dividers** | `#E5E5E5` | `(229, 229, 229)` | Card borders, horizontal dividers, and outline container strokes |
| **Primary Text** | `#111111` | `(17, 17, 17)` | Headlines, titles, item names, active filters, and high-contrast labels |
| **Secondary Text** | `#666666` | `(102, 102, 102)` | Subtitles, descriptions, domain tags, timestamps, and secondary metadata |
| **Tertiary Text / Hints** | `#888888` | `(136, 136, 136)` | Search placeholders, input hints, and subtle counter text |
| **Primary Button Container** | `#111111` | `(17, 17, 17)` | High-priority actions ("Manage", "Open in App", "Save", "Export", "Install") |
| **Primary Button Text** | `#FFFFFF` | `(255, 255, 255)` | White bold text on primary black buttons |
| **Secondary Button Container**| `#F2F2F2` | `(242, 242, 242)` | Secondary neutral actions ("Cancel", "Dismiss", "Clear", "Import") |
| **Secondary Button Text** | `#111111` | `(17, 17, 17)` | High-contrast dark text on secondary surface buttons |
| **Icons** | `#111111` | `(17, 17, 17)` | Navigation icons, action icons, section icons, and chevrons |
| **Disabled Elements** | `#A0A0A0` | `(160, 160, 160)` | Inactive/disabled buttons, toggles, and unavailable controls |

### 1.2 Official Dark Theme

| Design Element | Hex Code | RGB | Usage & Description |
| :--- | :--- | :--- | :--- |
| **Main Background** | `#000000` | `(0, 0, 0)` | Pure black main canvas |
| **Primary Surface** | `#171717` | `(23, 23, 23)` | Cards, dialog backdrops, bottom sheets, and top/bottom navigation bars |
| **Secondary Surface** | `#222222` | `(34, 34, 34)` | Inactive chips, input field backgrounds, and elevated secondary containers |
| **Borders & Dividers** | `#303030` | `(48, 48, 48)` | Subtle dark dividers, card outlines, and item separators |
| **Primary Text** | `#FFFFFF` | `(255, 255, 255)` | Headlines, titles, item names, and high-contrast primary typography |
| **Secondary Text** | `#B3B3B3` | `(179, 179, 179)` | Subtitles, descriptions, timestamps, and supporting metadata |
| **Tertiary Text / Hints** | `#757575` | `(117, 117, 117)` | Search placeholders and input hints |
| **Primary Button Container** | `#FFFFFF` | `(255, 255, 255)` | Inverted primary action buttons ("Manage", "Open in App", "Save", "Install") |
| **Primary Button Text** | `#111111` | `(17, 17, 17)` | High-contrast dark text on pure white primary buttons |
| **Secondary Button Container**| `#222222` | `(34, 34, 34)` | Secondary dark actions ("Cancel", "Dismiss", "Clear") |
| **Secondary Button Text** | `#FFFFFF` | `(255, 255, 255)` | Clean white text on secondary dark buttons |
| **Icons** | `#FFFFFF` | `(255, 255, 255)` | Navigation icons, action icons, section icons, and chevrons in dark mode |
| **Disabled Elements** | `#666666` | `(102, 102, 102)` | Inactive/disabled buttons, toggles, and unavailable controls in dark mode |

### 1.3 Strictly Prohibited Colors

The following colors are **strictly prohibited** anywhere in the Later Box user interface:

- **Yellow** (including `#FFF4B8`, `#F7E582`, `#FFD700`, etc.)
- **Amber / Gold / Bronze** (including `#F59E0B`, `#D97706`, `#B45309`, etc.)
- **Orange / Ochre / Brown** (including `#9E6F21`, `#9C6E1E`, `#EA580C`, etc.)
- **Blue / Cyan** (no blue links, no blue buttons, no blue chips)
- **Purple / Violet / Pink / Magenta**
- **Crimson / Bright Red Fonts** (destructive actions use neutral slate `#374151` / `#4B5563`, strictly zero red fonts)
- **Decorative Green**
- **Multicolor or Rainbow Gradients**

### 1.4 Destructive Actions (Zero Red Font Constraint)

| Token Name | Hex Code | Usage |
| :--- | :--- | :--- |
| **`SlateDestructive`** | `#374151` | Neutral slate for deletion and destructive confirmations (Light theme) |
| **`SlateDestructiveDark`** | `#4B5563` | Neutral slate for deletion and destructive confirmations (Dark theme) |

---

## 2. Typography Scale

The typography balances **editorial serif elegance** with **clean modern sans-serif legibility**.

### 2.1 Font Families
- **Display & Headings**: `FontFamily.Serif` (High-contrast editorial serif).
  - Used for screen titles (`Later Box`, `Library`, `Search`, `Your playlists`, `Recent saves`).
  - Used for empty state titles (`Nothing saved yet`, `Your library is empty`).
- **Body & Functional Microcopy**: `FontFamily.Default` (Clean neutral Sans-Serif).
  - Used for metadata, subtitles, filter labels, button labels, inputs, and navigation items.

### 2.2 Typography Hierarchy

| Style | Font Family | Weight | Size | Line Height | Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Display Large** | Serif | Bold | `34.sp` | `40.sp` | Main hero headings |
| **Display Medium** | Serif | Bold | `32.sp` | `38.sp` | Screen headers (`Library`, `Search`) |
| **Headline Large** | Serif | Bold | `28.sp` | `34.sp` | Major section headers |
| **Headline Medium** | Serif | Bold | `24.sp` | `30.sp` | Modal and card titles |
| **Headline Small** | Serif | SemiBold | `20.sp` | `26.sp` | Content detail titles, card titles |
| **Title Medium** | Sans | SemiBold | `15.sp` | `20.sp` | Compact list titles, dialog titles |
| **Body Large** | Sans | Normal | `15.sp` | `22.sp` | Article excerpts, reader content |
| **Body Medium** | Sans | Normal | `14.sp` | `20.sp` | Descriptions, empty state subtitles |
| **Body Small** | Sans | Normal | `12.sp` | `16.sp` | URLs, domains, relative dates |
| **Label Large** | Sans | SemiBold | `13.sp` | `16.sp` | Primary and Manage buttons |
| **Label Small** | Sans | Medium | `11.sp` | `14.sp` | Bottom navigation bar labels |

---

## 3. Core Component Blueprints

### 3.1 Manage Button & Primary Action Buttons
- **Purpose**: High-visibility interactive triggers ("Manage", "Open in App", "Save", "Export", "Install").
- **Light Theme**:
  - Container Background: `#111111` (Solid Black)
  - Text Color: `#FFFFFF` (Pure White)
  - Icon Tint: `#FFFFFF` (Pure White)
  - Border: None
- **Dark Theme**:
  - Container Background: `#FFFFFF` (Pure White)
  - Text Color: `#111111` (Solid Black)
  - Icon Tint: `#111111` (Solid Black)
  - Border: None
- **Geometry**:
  - Shape: `RoundedCornerShape(10.dp)`
  - Minimum Touch Target: `48.dp` height × `48.dp` width
  - Padding: `14.dp` horizontal × `10.dp` vertical
  - Bounds Clipping: Strict `.clip(RoundedCornerShape(10.dp))` preventing any color bleed.
- **States**:
  - Pressed: Standard Material ripple with high contrast.
  - Disabled: `alpha = 0.4f` container and content.

### 3.2 5-Destination Bottom Navigation Bar
- **Container**:
  - Light: `#FFFFFF` background with `1.dp` solid top border in `#E5E5E5`.
  - Dark: `#171717` background with `1.dp` solid top border in `#303030`.
- **Destinations**: Home, Library, Save (+), Search, Settings.
- **Save (+) Center Action**:
  - Elevated circular badge (`42.dp`) with `#111111` (Light) / `#FFFFFF` (Dark) fill.
  - Plus icon in `#FFFFFF` (Light) / `#111111` (Dark).
- **Active Destination**:
  - Light: `#111111` icon and label with subtle `#F2F2F2` capsule background.
  - Dark: `#FFFFFF` icon and label with subtle `#222222` capsule background.
- **Inactive Destination**:
  - Light: `#666666` icon and label.
  - Dark: `#B3B3B3` icon and label.

### 3.3 Cards and Content Feed
- **Surface**:
  - Light: `#FFFFFF` background with `1.dp` solid border in `#E5E5E5`.
  - Dark: `#171717` background with `1.dp` solid border in `#303030`.
- **Corner Radius**: `14.dp` or `16.dp`.
- **Badge / Tag Pills**:
  - Light: Container `#F2F2F2`, Text `#111111`, Border `#E5E5E5`.
  - Dark: Container `#222222`, Text `#FFFFFF`, Border `#303030`.
- **Video Play Overlay**:
  - Circular badge (`44.dp`) with `#111111` (Light) / `#FFFFFF` (Dark) fill and `#FFFFFF` (Light) / `#111111` (Dark) play icon.

### 3.4 Filter Pills & View Toggles
- **Filter Pills**:
  - Active: `#111111` container, `#FFFFFF` text (Light) / `#FFFFFF` container, `#111111` text (Dark).
  - Inactive: `#F2F2F2` container, `#111111` text (Light) / `#222222` container, `#FFFFFF` text (Dark).
- **View Toggle (Grid | List)**:
  - Container: `#FFFFFF` / `#171717` capsule with `1.dp` border.
  - Active segment: `#111111` / `#FFFFFF` fill with inverted text.

### 3.5 Dialogs and Sheets
- **Container**:
  - Light: `#FFFFFF` background, `16.dp` rounded corners, `#E5E5E5` border.
  - Dark: `#171717` background, `16.dp` rounded corners, `#303030` border.
- **Confirmation Action**: Primary button (`#111111` in Light, `#FFFFFF` in Dark).
- **Dismiss Action**: Text button with `#666666` in Light, `#B3B3B3` in Dark.
- **Radio Buttons & Selection**:
  - Selected: `#111111` (Light) / `#FFFFFF` (Dark).
  - Unselected: `#666666` (Light) / `#B3B3B3` (Dark).

### 3.6 Settings & Gemini AI Interface
- **Section Headers**: `#111111` (Light) / `#FFFFFF` (Dark) with matching icon tint.
- **Status Badges**:
  - Active: `#F2F2F2` (Light) / `#222222` (Dark) container with `#111111` / `#FFFFFF` text and `1.dp` border.
  - Inactive: `#F2F2F2` container with `#666666` text.
- **Settings Manage Buttons**: Standard 48dp monochrome Manage button.
- **Update Status Pill**:
  - Up-to-date: `#F2F2F2` / `#222222` pill with `#111111` / `#FFFFFF` text.
  - Update available: `#111111` / `#FFFFFF` badge with inverted text.

---

## 4. Accessibility and Theme Consistency

1. **Contrast Ratio**:
   - Primary text (`#111111` on `#FFFFFF` / `#FFFFFF` on `#000000`) achieves `16.5:1` to `21:1` (exceeds WCAG AAA).
   - Secondary text (`#666666` on `#FFFFFF` / `#B3B3B3` on `#000000`) achieves `5.7:1` to `12.6:1` (exceeds WCAG AA).
   - Primary buttons achieve `16.5:1` to `21:1` contrast in both themes.
2. **Touch Targets**: All interactive elements maintain a minimum bounding box of `48.dp × 48.dp`.
3. **Adaptive Switching**: Both themes are unified through `ThemeMode.SYSTEM`, `ThemeMode.LIGHT`, and `ThemeMode.DARK`. No hardcoded colors override theme tokens.
4. **Zero Yellow / Monochrome Compliance**: All screens, dialogs, sheets, and notification components follow the monochrome palette strictly.
