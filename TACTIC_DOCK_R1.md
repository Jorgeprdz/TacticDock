# Tactic Dock R1 — Functional Samsung DeX MVP
Source forked from [Smart Dock](https://github.com/axel358/smartdock) under GPL-3.0.
Retain upstream copyright and GPL-3.0 attribution and redistribution obligations.

## Scope
- New Android application ID: `mx.tactic.dock`, allowing installation alongside original Smart Dock.
- Bottom-centered, content-sized floating overlay on the external display when detected.
- Existing pinned and running app model, app drawer, right-click/long-press.
- Floating translucent rounded container with elevation.
- Does not replace Android launcher or alter Samsung SystemUI / taskbar.
- Supports existing DeX screen; actual Android 17 behavior requires hardware testing.

## Physical test on Samsung DeX
1. Download the APK from a successful GitHub Actions workflow, and install on test phone.
2. Grant requested overlay and accessibility permissions.
3. Enable native DeX taskbar auto-hide if available.
4. Connect an external monitor; verify centering and buttons.
5. Pin apps including Archium, FinderDex, Tactic Terminal; launch and switch tasks.
6. Test mouse right-click, reconnection, screen resolution changes.
7. Check collision with DeX taskbar when pointer hits screen edge.
8. If broken, disable accessibility service or uninstall Tactic Dock.

## Deferred
- True live background blur, hover magnification, taskbar collision fixes,
  responsive icon size and premium animation are NOT implemented in R1.
- Build status and APK availability are confirmed ONLY by GitHub Actions, not by this commit.

## R1.2 — Floating macOS-style glass dock
- No auto-hide or trigger at the system taskbar edge; persistent dock, y=26dp.
- Frosted glass capsule with translucent light/dark gradient, soft border and elevation.
- Request Android 12+ cross-window blur (system-dependent, translucency fallback).
- 48dp native app icons, a gentle 1.15x mouse hover, indicator dots.
- Preserves Samsung DeX taskbar; use One UI's own auto-hide setting.
- Device-side verification is still pending.
