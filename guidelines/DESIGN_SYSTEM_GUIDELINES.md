# Design Coherence Rules (Simple)

Purpose: Keep the app visually consistent, accessible, and easy to maintain. These rules are language- and framework-agnostic.

---

## Core

- Use project design tokens for all visuals: colors, fonts, spacing, radii, shadows.
- Do not hardcode values (no literal hex/rgb, pixel sizes, or ad‑hoc shadows).
- Use semantic roles (background, text, border, interactive, status) instead of raw palette values.
- Keep a clear hierarchy with predefined typography styles.
- Use spacing scale for padding, margins, and gaps; avoid arbitrary numbers.

---

## States & Interaction

- Implement all states: default, hover, active, focus, disabled, error.
- Ensure focus-visible styles are present and consistent.
- Use shared transition timings/easing; avoid custom animations per component.

---

## Accessibility

- Meet WCAG AA contrast: 4.5:1 for body text; 3:1 for large text/UI.
- Keyboard support: tab order logical, focus indicators visible.
- Touch targets: minimum 44×44 (or platform equivalent).
- Provide descriptive labels/ARIA where applicable.

---

## Responsiveness & Themes

- Follow project breakpoints for layout; avoid fixed widths.
- Support light and dark themes using semantic tokens (no manual color overrides).
- Use responsive container widths and consistent padding.

---

## Icons

- Use the approved icon set and standard sizes (e.g., 16/20/24/32/48).
- Inherit color via currentColor and include accessible labels.

---

## Do & Don't

- Do reference tokens for all visual values.
- Do keep spacing/typography rhythm consistent across screens.
- Don't introduce new visual variants without design review.
- Don't bypass tokens with one-off values.

---

## Quick Checklist (Before Merge)

- Colors use semantic tokens; no hardcoded values.
- Typography uses defined styles and font families.
- Spacing follows the scale; consistent gaps/margins.
- States (default/hover/active/focus/disabled/error) implemented.
- Accessibility (contrast AA, focus-visible, labels, touch targets) verified.
- Responsive across breakpoints; supports light/dark themes.

---

| Version | Date | Changes |
|---------|------|---------|
| 1.2 | 2026-01-26 | Simplified, language-agnostic rules |
| 1.1 | 2026-01-26 | Condensed framework-agnostic core rules |
| 1.0 | 2026-01-26 | Initial comprehensive design system guidelines |
# Design System Guidelines

Extends ROOT_GUIDELINES.md. Covers design system standards including colors, typography, spacing, components, and accessibility.