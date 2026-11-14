# Android System Control X

Android system utility built for Shizuku. Initial target: Android 11 / MIUI 12.5.

## Initial features
- Shizuku connection and permission status
- Animation scale presets (0.5x, 1x, off)
- Device information
- Shortcuts to Android settings
- Installed-app list with app details shortcut

## Build
GitHub Actions runs on push to main or manually via workflow_dispatch. Download the APK from the workflow run's Artifacts section.

## Safety
This is an initial debug build. Review every action before use. Some settings may be restricted by Android or MIUI. No root access is required for the supported Shizuku commands.
