# Android Device Insights App
## UI / UX Design Guidelines

This document translates the functional specifications into concrete design rules, visual language, and screen-by-screen UI requirements. It is intended for designers, frontend engineers, and product owners to stay aligned.

---

## 1. Design Goals

1. Clarity over complexity: complex metrics must feel understandable at a glance.
2. Action-oriented insights: every screen should answer "Is something wrong?" and "What can I do?".
3. Trust & credibility: data visualization should feel technical, accurate, and non-gimmicky.
4. Progressive disclosure: basic users see summaries, power users can drill down.
5. Privacy-first perception: UI should visibly communicate on-device processing and user control.

---

## 2. Visual Identity

### 2.1 Color System

Base theme: Material You compliant, supports dynamic color on Android 12+.

#### Core Semantic Colors

- Success / Good: Green
- Warning / Attention: Amber
- Critical / Risk: Red
- Neutral / Informational: Blue
- Disabled / Inactive: Gray

Usage rules:
- Never rely on color alone; always pair with labels or icons.
- Health scores always map to a gradient (red → amber → green).
- Charts must use muted tones; alerts may use saturated colors.

#### Backgrounds

- Primary background: Surface / SurfaceVariant
- Cards: Elevated surface with subtle shadow
- Critical alerts: tinted background (low opacity)

---

### 2.2 Typography

- Font: System default (Roboto / Google Sans via Material)
- Headings: Medium to SemiBold
- Body text: Regular
- Numeric data: Use tabular figures where possible

Hierarchy example:
- Screen title
- Section title
- Metric value
- Supporting explanation

---

### 2.3 Spacing & Layout

- Base spacing unit: 8dp
- Card padding: 16dp
- Section spacing: 24dp
- Avoid dense layouts; prefer vertical scrolling

---

## 3. Navigation Model

### 3.1 Primary Navigation

Bottom Navigation (5 items max):
- Dashboard
- Insights
- History
- Tools
- Settings

Rules:
- No hidden critical features
- Badges allowed only for alerts or actions

### 3.2 Secondary Navigation

- Tabs for sub-features (Battery, Thermal, Performance, etc.)
- Drill-down via standard navigation stack

---

## 4. Global UI Components

### 4.1 Health Score Card

Appears on Dashboard and feature screens.

Shows:
- Numeric score (0–100)
- Status label (Good / Warning / Critical)
- Trend indicator (up / down)

---

### 4.2 Insight Card

Structure:
- Title
- Severity badge
- Short explanation
- Expected impact
- Action button

Rules:
- One primary action only
- Dismiss option always available

---

### 4.3 Charts

- Time ranges: 24h / 7d / 30d / 90d / 1y
- Interactive: pan + zoom
- Tooltip on tap

Rules:
- No more than 3 datasets per chart
- Default view always shows recent data

---

## 5. Screen-by-Screen Guidelines

### 5.1 Onboarding Screen

Purpose:
- Explain value
- Build trust
- Request permissions progressively

Must show:
- What the app does
- What data is collected (high-level)
- Benefits of granting permissions

Avoid:
- Technical jargon
- Requesting all permissions at once

---

### 5.2 Dashboard (Home)

Purpose:
- Single-glance device health overview

Must show:
- Overall Health Score
- Battery, Thermal, Performance mini-cards
- Active critical alerts
- Top 1–2 recommendations

Optional:
- Gamification progress

---

### 5.3 Battery Screen

Purpose:
- Explain battery condition and habits

Must show:
- Current status (level, temp, charging)
- Battery health score
- Capacity vs design
- Drain by top apps

Expandable sections:
- Charging sessions
- Temperature trends
- Aging forecast

---

### 5.4 Thermal Screen

Purpose:
- Prevent overheating and throttling

Must show:
- Current thermal state
- Recent thermal events
- Temperature trend chart

Advanced view:
- Component temperature breakdown
- Throttling impact

---

### 5.5 Performance Screen

Purpose:
- Identify slowdowns and jank

Must show:
- Performance score
- Recent lag or ANR events
- App performance ranking

Advanced:
- Frame metrics
- Memory pressure timeline

---

### 5.6 Storage Screen

Purpose:
- Help users reclaim space safely

Must show:
- Used vs free storage
- Largest categories
- Top cleanup opportunities

Advanced:
- Duplicate files
- Storage growth forecast

---

### 5.7 Network Screen

Purpose:
- Improve connectivity efficiency

Must show:
- Current connection quality
- Signal strength
- Data usage summary

Advanced:
- Speed test history
- App data consumption

---

### 5.8 Screen Health Screen

Purpose:
- Protect display longevity

Must show:
- Screen-on time
- Brightness usage
- Burn-in risk level

Recommendations:
- Dark mode
- Brightness adjustments

---

### 5.9 Sensors Health Screen

Purpose:
- Diagnose hardware sensors

Must show:
- Sensor list
- Status (OK / Needs calibration)

Actions:
- Run calibration tests

---

### 5.10 Audio Health Screen

Purpose:
- Protect hearing and audio hardware

Must show:
- Headphone volume exposure
- Speaker health status

Alerts:
- Safe listening warnings

---

### 5.11 Insights Screen

Purpose:
- Central place for recommendations

Must show:
- Sorted by priority
- Clear impact explanation

Filters:
- Battery / Performance / Storage / Network

---

### 5.12 History Screen

Purpose:
- Visualize trends over time

Must show:
- Selectable metric
- Time range controls

---

### 5.13 Reports & Export Screen

Purpose:
- Share diagnostics

Must show:
- Report types
- Export formats

UX rule:
- Always preview before export

---

### 5.14 Automation Screen

Purpose:
- Configure smart actions

Must show:
- Existing automations
- Templates

Builder:
- If / Then / Else visual flow

---

### 5.15 Settings & Privacy Screen

Purpose:
- User control and trust

Must show:
- Data collection toggles
- Permissions status
- Export / delete data

---

## 6. States & Feedback

- Loading: skeletons, never spinners only
- Empty: explain why data is missing
- Error: human-readable, actionable
- Success: subtle confirmation

---

## 7. Accessibility

- Minimum contrast: WCAG AA
- Scalable text
- Screen reader labels for all metrics
- No critical info conveyed by color alone

---

## 8. Do / Don’t Summary

Do:
- Explain metrics in plain language
- Show impact of actions
- Let users drill down

Don’t:
- Overwhelm first-time users
- Use fear-based messaging
- Hide permission rationale

---

This document should be used as the baseline for Figma designs, Jetpack Compose implementation, and UX revi