# core/design-system (legacy Android-only)

Legacy Android-only design-system module.

## Status

This module is **not** the primary cross-platform UI path anymore. Shared design tokens and theme are now mainly in `:shared-ui`.

## Contains

- Material theme wrappers
- Color and typography definitions
- Font assets (Vazir / Vazirmatn)
- Utility extensions used by older Android-only screens

## Keep/Remove Guidance

- Keep only if another active module still depends on these resources.
- Prefer new UI work in `:shared-ui`.
