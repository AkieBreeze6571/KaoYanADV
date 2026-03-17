## Example 1: Quest board list screen

**User input**
- Target screen: `QuestBoardScreen`
- File: `app/src/main/java/.../QuestBoardScreen.kt`
- Vibe: quest board
- Constraints: no new deps; keep existing logic

**Expected response shape**

Plan:
- Briefly describe current structure and the biggest readability issues.
- Convert list container to `LazyColumn` with consistent `contentPadding`.
- Switch rows to `OutlinedCard` with a consistent clickable surface and padding.
- Add a clear title/subtitle hierarchy via `MaterialTheme.typography`.
- Add subtle dividers/metadata chips using existing data only.
- Ensure touch targets and icon `contentDescription`.

Patch:
- Provide a minimal Kotlin patch for `QuestBoardScreen.kt` that only adjusts presentation/modifiers/typography.

What changed:
- Call out the file(s) changed, before/after layout hierarchy, and confirm state flow is untouched.

Test checklist:
- Launch screen; scroll; tap each quest row; verify navigation/events unchanged.
- Verify text truncation/ellipses look good; font sizes readable.
- Verify TalkBack reads headings and icons appropriately.

---

## Example 2: Battle report details screen

**User input**
- Target screen: `BattleReportScreen`
- File: `app/src/main/java/.../BattleReportScreen.kt`
- Vibe: battle report
- Constraints: keep existing state & logic

**Expected response shape**

Plan:
- Summarize current structure (`Scaffold` → `Column` etc.) and identify hierarchy issues.
- Add a summary `Card` at top with aligned stat rows.
- Improve spacing and typography tokens; reduce visual noise.
- Group sections with headings (semantics heading where appropriate).
- Ensure buttons/rows meet touch target expectations.
- Keep colors strictly from `MaterialTheme.colorScheme`.

Patch:
- Minimal Kotlin patch updating modifiers, typography, and card/list presentation only.

What changed:
- “Before vs after” in a few bullets; confirm no logic changes.

Test checklist:
- Verify all actions still work (share, back, retry, etc. if present).
- Verify numbers align and remain the same; no data changes.
- Verify dark/light theme consistency.

