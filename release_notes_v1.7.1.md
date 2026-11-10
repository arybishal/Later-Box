# Later Box v1.7.1 — Dynamic Gemini Model Discovery & Diagnostic Architecture

## Overview
Later Box **v1.7.1** introduces a fully dynamic model discovery and management system for Google's Gemini Developer API, replacing static model registries with live API querying, pagination support, search and family filtering, and a robust 6-step connection diagnostic suite.

---

## What's New in v1.7.1

### 1. Dynamic Gemini Model Discovery (`models.list`)
- **Official API Integration:** Discovers all accessible models in real-time via `GET https://generativelanguage.googleapis.com/v1beta/models` using standard `x-goog-api-key` header authentication.
- **Pagination Support:** Seamlessly iterates across paginated responses using `nextPageToken` (up to 1,000 models).
- **Capability Filtering:** Intelligently screens models to ensure only those supporting the required `generateContent` method are presented for saved content analysis, preventing incompatible embedding or text-only models from causing runtime failures.
- **Future-Proof Family Support:** Dynamically renders newer and experimental model families (e.g. `gemini-2.5-flash`, `gemini-2.0-flash`, `gemini-2.5-flash-image`, etc.) without requiring code updates.

### 2. Redesigned Model Selector & Family Filtering
- **Real-Time Model Search:** Instantly filters models by name, model ID, or description keywords.
- **Model Family Chips:** Categorize models across `All`, `Flash`, `Pro`, and `Other / Experimental` with live counter badges.
- **Metadata Cards:** Displays model display name, exact API resource identifier in monospace (`id`), input/output token limits (e.g. `1M in · 8K out`), and description.
- **Intelligent Recommendation:** Dynamically highlights the best general-purpose Flash model with a soft butter-yellow `RECOMMENDED` badge while keeping all user options available.
- **Unavailable Model Warning:** If a previously selected model is not available in the user's project, displays a clear warning card with a one-tap option to switch to the recommended model without silently altering user settings.

### 3. 6-Step Connection Diagnostics
- **Step 1:** Validate API key format and non-empty state.
- **Step 2:** Query `models.list` to discover accessible project models.
- **Step 3:** Verify target model exists within the accessible project models.
- **Step 4:** Verify target model supports `generateContent`.
- **Step 5:** Send minimal ping prompt (`Respond with 'OK'`) via secure header authentication.
- **Step 6:** Validate structured candidate output returned by Google's API.
- **Granular Error Handling:** Correctly distinguishes `INVALID_KEY`, `API_NOT_ENABLED`, `API_KEY_RESTRICTED`, `MODEL_NOT_FOUND`, `MODEL_UNSUPPORTED`, `QUOTA_EXCEEDED`, `RATE_LIMIT`, `BILLING_RESTRICTION`, and explicit `REGIONAL_RESTRICTION`. HTTP 404 is never misdiagnosed as a regional restriction.

### 4. Subscription vs. Developer API Clarification Banner
- Adds a prominent notice in Settings → AI & Intelligence explaining:
  > *"Your Google AI subscription and Gemini Developer API usage are separate. API access, available models, quotas and charges depend on your Google API project and its configuration."*

### 5. Strict Visual Identity Compliance
- Built with Later Box's signature palette:
  - Clean Background: `#FFFFFF`
  - Typography & Borders: `#111111`
  - Soft Butter Yellow Highlight: `#FFF4B8`
  - Muted Inactive Pill: `#F6F6F6`
- Zero prohibited colors (`#9E6F21`, red fonts, brown, gold, or neon colors).

---

## Checksums & Artifacts

| Artifact | Size | SHA-256 Checksum |
| :--- | :--- | :--- |
| `app-release.apk` | 15.9 MB | `EDE6AD2B2401D2D5EA9A6820FFF1FAA239CC12D212D1FBF20B148131D53667B1` |
| `app-debug.apk` | 23.2 MB | `F12807AB17E8BF41E06D9650B479D296FBEA8BDD3D247E8FA6FCBA95175BE42C` |
