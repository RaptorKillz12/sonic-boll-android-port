# Sonic Boll Android Port

This repository is being set up as a Winlator-based Android launcher/fork project for Sonic Boll.

## Goal

Use the Winlator source code as the base and add a custom startup flow that:

- downloads the Sonic Boll game files from Dropbox
- extracts them automatically
- configures the game profile
- launches the game directly inside Winlator
- removes the need for manual setup on first run

## Architecture

This is not a “native Android port” yet. It is a Winlator fork/customized launcher.

The flow is:

1. app starts
2. check whether Sonic Boll files already exist
3. if missing, download ZIP from Dropbox
4. extract ZIP to the Winlator game directory
5. create or update the Winlator profile for Sonic Boll
6. launch the game inside the Winlator environment

## Project structure

- `android/` – starter Android app scaffold for the launcher
- `docs/` – architecture notes and launch-plan documentation

## Important note

The actual Winlator source code must be checked out and patched in a forked project. This repository contains the Android app scaffolding needed to implement the Sonic Boll startup logic and launch integration.

## Current scaffold

- Android app module with Kotlin launcher activity
- permission setup
- Dropbox download hook
- ZIP extraction logic
- Winlator launch intent placeholder
- custom startup flow ready to be adapted to the real Winlator source

## Next steps

1. fork the actual Winlator source code
2. replace the placeholder Winlator package name with the real package
3. confirm the container path used by Winlator
4. add the exact Sonic Boll executable path
5. wire the startup flow to the real Winlator launcher code

