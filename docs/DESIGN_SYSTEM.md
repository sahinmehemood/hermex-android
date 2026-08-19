# Optimus Design System

## Visual direction

Optimus should feel like a native premium client, not a web page inside a phone.

Primary references:

- Official Hermes desktop UI for information hierarchy and functional density.
- High-quality native iOS/SwiftUI clients for motion, sheets, hierarchy, spacing, and interaction polish.
- Modern Android Compose conventions for accessibility, predictive back, dynamic colors, and adaptive layouts.

## Surfaces

Use a small semantic surface scale:

- `background` — application canvas.
- `surface` — primary content plane.
- `surfaceVariant` — supporting panels.
- `raised` — transient cards and tool events.
- `scrim` — modal backdrop.

Avoid decorative gradients unless they communicate a state or focus. Prefer depth through spacing, subtle tonal shifts, and restrained elevation.

## Typography

- Primary UI typeface: system sans / Roboto unless a product-specific asset is intentionally added.
- Display sizes are used sparingly.
- Chat prose prioritizes readability over compactness.
- Code uses a monospace face with horizontal scrolling or wrapping according to content type.

## Components

Core reusable primitives:

- `OptimusTopBar`
- `ConversationMessage`
- `StreamingMessage`
- `ToolEventCard`
- `ApprovalCard`
- `ClarificationCard`
- `Composer`
- `SessionRow`
- `ConnectionStatusChip`
- `ModelPickerSheet`
- `ProfilePickerSheet`
- `AdaptiveNavigation`
- `EmptyState`
- `ErrorState`
- `OfflineBanner`
- `ProgressPill`
- `WorkspaceFileRow`

Components should expose semantics, content descriptions, and test tags where useful.

## Motion

- Use short, physically plausible transitions for local navigation.
- Use spring-based expansion for tool cards and sheets where it improves continuity.
- Streaming text should update without full-layout flicker.
- Preserve scroll position when tool events append.
- Respect reduced-motion settings.

## Responsive rules

Phone:
- bottom navigation or compact top-level navigation;
- full-screen chat;
- modal sheets for secondary selectors.

Tablet/foldable:
- persistent navigation rail/sidebar;
- two-pane session + conversation layout;
- optional workspace/tool inspector pane.

Landscape phone:
- collapse secondary chrome;
- protect composer from IME overlap;
- maintain readable text width.

## State visuals

Every feature needs intentional visuals for:

- loading;
- empty;
- offline;
- reconnecting;
- partial/streaming;
- approval required;
- success;
- non-fatal warning;
- terminal error.
