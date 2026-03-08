# core/ui/common (legacy Android-only)

Legacy Android-only reusable UI components.

## Status

This module mostly overlaps with components now migrated to `:shared-ui`.

## Contains

- Common Compose components (banner, cart item, search app bar, shimmer, etc.)
- Legacy common routes/state holders
- Language selection UI (legacy Android-only version)

## Keep/Remove Guidance

- Use for backward compatibility only while completing migration.
- Prefer shared implementations in `:shared-ui` for all new changes.
