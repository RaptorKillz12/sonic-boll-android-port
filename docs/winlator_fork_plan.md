# Project notes for the Winlator fork plan

## Winlator fork strategy

This project is intended to be built on top of the actual Winlator Android source code, not as a separate app that merely calls a prebuilt binary.

## Startup flow

1. user launches the custom Winlator fork
2. app checks for Sonic Boll install directory
3. if missing, downloads the Dropbox ZIP
4. extracts to the proper Winlator gaming directory
5. creates the game launch profile or config
6. directly starts the game executable inside the Winlator environment

## File locations

- game archive: Dropbox URL
- install target: `/Android/data/<package>/files/sonic_boll/`
- launch target: the Sonic Boll executable inside the extracted directory

## Integration points in Winlator

The real Winlator source will need to be patched in the startup activity and launcher flow to insert the Sonic Boll bootstrap logic before the standard game selection screen is shown.

## Why this matters

This preserves the original Winlator runtime while customizing the launch experience to automatically install and run Sonic Boll without a manual setup process.
