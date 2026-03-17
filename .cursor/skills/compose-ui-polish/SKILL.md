---
name: compose-ui-polish
description: Polishes Jetpack Compose UI screens (layout, spacing, typography, component consistency, and theme usage) to feel more RPG/adventure-like and more readable, without changing business logic or state flow. Use when asked to “polish UI”, “make Compose screen nicer/clearer/consistent”, “improve typography/spacing”, or when targeting vibes like guild menu, quest board, or battle report.
---

# Compose UI Polish

## Goal

Polish existing Jetpack Compose screens to improve **layout**, **typography**, **spacing**, **visual hierarchy**, and **Material theme consistency**, while preserving:
- Existing features and behavior
- Existing state and event flow
- Existing data (do not invent new data; presentation-only changes are allowed)
- No new libraries/dependencies

Prefer editing only the necessary screen files under `app/src/main/...`. Avoid touching Gradle unless absolutely necessary.

## Inputs the user will provide

- Target screen name(s) and file path(s)
- Desired vibe (examples: RPG guild menu, quest board, battle report)
- Constraints (examples: no new dependencies; keep existing state/logic)

If any are missing, infer a reasonable default vibe from the screen purpose and keep changes conservative.

## Strict output format

Your final response MUST have exactly these top-level sections (in this order):

- Plan:
- Patch:
- What changed:
- Test checklist:

Put all content under these headings. Keep the **Plan** to a maximum of 6 bullet points.

## Process (must follow)

1) **Read the existing Composable(s)** from the provided paths.
   - Briefly list the current UI structure (e.g., `Scaffold` → `TopAppBar` → `Column` → `LazyColumn` items).
   - Note obvious issues: inconsistent padding, dense text, weak hierarchy, hard-to-tap elements, color mismatches.

2) **Propose a UI plan** (max 6 bullets).
   - Mention specific components to adjust (e.g., `Card`/`OutlinedCard`, list rows, chips/tags, dividers, `TopAppBar`, spacing, typography).
   - Tie choices to the requested vibe (e.g., “quest board”: framed cards, section headers, subtle dividers, consistent row height).

3) **Provide a concrete patch (Kotlin code)** with minimal changes.
   - Preserve behavior; do not refactor state holders, navigation, ViewModels, or business logic.
   - Prefer small composable-local changes: modifiers, arrangement, typography tokens, colors from `MaterialTheme`.
   - Keep the patch realistic for the current codebase (reuse existing styles/components if present).

4) **Accessibility basics (required)**
   - Ensure readable font sizes (avoid tiny text; use `MaterialTheme.typography.*`).
   - Ensure sufficient spacing and touch targets (aim for ~48.dp tap targets for clickable rows/buttons when feasible).
   - Ensure content hierarchy: clear title/subtitle/body separation; consistent alignment.
   - Provide `contentDescription` for meaningful icons; use `semantics { heading() }` for key section titles when appropriate.
   - Avoid conveying meaning only by color (add label text or icon where needed).

5) **Before/after summary + manual test checklist**
   - Summarize what changed and what did NOT change (state/logic).
   - Provide a short checklist to verify visually and functionally.

## Default UI polish tactics (use as needed)

- **Layout & spacing**
  - Standardize outer padding (commonly 16.dp).
  - Use `Arrangement.spacedBy(8.dp/12.dp/16.dp)` instead of manual `Spacer` spam (or use `Spacer` consistently).
  - Prefer `LazyColumn(contentPadding = PaddingValues(...), verticalArrangement = spacedBy(...))` for lists.
  - Make rows fully clickable with `Modifier.fillMaxWidth().clickable(...)` and stable padding.

- **Typography & hierarchy**
  - Use `MaterialTheme.typography.titleLarge/titleMedium/bodyLarge/bodyMedium/labelLarge`.
  - Use `FontWeight.SemiBold` sparingly for headings; avoid bolding everything.
  - Clamp text lines with `maxLines` and `overflow = TextOverflow.Ellipsis` where appropriate.

- **Theme consistency**
  - Use `MaterialTheme.colorScheme` and `MaterialTheme.typography` instead of hard-coded colors/sizes.
  - Prefer `CardDefaults` for elevation and colors; keep tonal elevation subtle.

- **RPG/adventure vibe (without new deps)**
  - “Guild menu”: clear section headers, icon-leading rows, framed/outlined cards, consistent item height.
  - “Quest board”: parchment-like feel via `OutlinedCard` + tonal surface + dividers; strong titles and metadata chips.
  - “Battle report”: compact summary card + stat rows with alignment, emphasized key numbers, clear grouping.

## Guardrails (do not do)

- Do NOT add new dependencies, fonts, or images.
- Do NOT change navigation routes, analytics, network calls, ViewModel logic, repositories, or domain models.
- Do NOT invent new fields/data; only rearrange existing info and adjust existing strings for clarity.
- Do NOT perform large refactors; keep diffs small and localized to the target screen(s).

## Patch expectations

In the **Patch:** section, include a patch that is directly applicable to the provided file paths. Make it minimal and focused on presentation changes.

## Additional resources

- For a couple of end-to-end examples (prompt → response), see [examples.md](examples.md).

