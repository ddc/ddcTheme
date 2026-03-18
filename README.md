<h1 align="center">
  <img src="https://raw.githubusercontent.com/ddc/JetbrainsTheme/refs/heads/master/assets/ddcSoftwaresThemesIcon.svg" alt="ddcSoftwaresThemesIcon" width="150">
  <br>
  DDC Jetbrains Theme
</h1>

<p align="center">
    <a href="https://github.com/sponsors/ddc"><img src="https://img.shields.io/static/v1?style=plastic&label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=ff69b4" alt="Sponsor"/></a>
    <br>
    <a href="https://ko-fi.com/ddc"><img src="https://img.shields.io/badge/Ko--fi-ddc-FF5E5B?style=plastic&logo=kofi&logoColor=white&color=brightgreen" alt="Ko-fi"/></a>
    <a href="https://www.paypal.com/ncp/payment/6G9Z78QHUD4RJ"><img src="https://img.shields.io/badge/Donate-PayPal-brightgreen.svg?style=plastic&logo=paypal&logoColor=white" alt="Donate"/></a>
    <br>
    <a href="https://plugins.jetbrains.com/plugin/30414-ddc-theme"><img src="https://img.shields.io/jetbrains/plugin/d/30414?style=plastic&logo=jetbrains&logoColor=white" alt="Marketplace Downloads"/></a>
    <a href="https://github.com/ddc/JetbrainsTheme/blob/master/LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic&logo=apache&logoColor=white" alt="License: Apache 2.0"/></a>
    <a href="https://github.com/ddc/JetbrainsTheme/releases/latest"><img src="https://img.shields.io/github/v/release/ddc/JetbrainsTheme?style=plastic&logo=github&logoColor=white" alt="Release"/></a>
    <br>
    <a href="https://github.com/ddc/JetbrainsTheme/issues"><img src="https://img.shields.io/github/issues/ddc/JetbrainsTheme?style=plastic&logo=github&logoColor=white" alt="issues"/></a>
    <a href="https://github.com/ddc/JetbrainsTheme/actions/workflows/workflow.yml"><img src="https://img.shields.io/github/actions/workflow/status/ddc/JetbrainsTheme/workflow.yml?style=plastic&logo=github&logoColor=white&label=CI%2FCD%20Pipeline" alt="CI/CD Pipeline"/></a>
    <a href="https://actions-badge.atrox.dev/ddc/JetbrainsTheme/goto?ref=master"><img src="https://img.shields.io/endpoint.svg?url=https%3A//actions-badge.atrox.dev/ddc/JetbrainsTheme/badge?ref=master&label=build&logo=github&style=plastic" alt="Build Status"/></a>
</p>

<p align="center">A dark theme for JetBrains IDEs based on <a href="https://github.com/atom/atom/tree/master/packages/one-dark-ui">Atom One Dark</a> colors.<br>Includes UI Theme, Editor Theme, VCS Colors, Keymaps, Code Style, Window Layout, and Selection Occurrence Highlighting.</p>

<p align="center">📦 <b><a href="https://plugins.jetbrains.com/plugin/30414-ddc-theme">Install from JetBrains Marketplace</a></b> 📦 </p>

# Table of Contents

- [Screenshot](#screenshot)
- [Features](#features)
- [Installation](#installation)
    - [From Marketplace](#from-marketplace)
    - [From Plugin ZIP](#from-plugin-zip)
- [Getting Started](#getting-started)
- [Building](#building)
- [Keymaps Changes](#keymaps-changes)
- [Version Control File Status Colors](#version-control-file-status-colors)
- [License](#license)
- [Support](#support)

# Screenshot

<p align="left">
  <img src="assets/example_bash.png" alt="Editor Theme">
</p>

# Features

| Component                   | Description                                                                                                    |
|-----------------------------|----------------------------------------------------------------------------------------------------------------|
| UI Theme                    | Dark UI with custom backgrounds, borders, and popups                                                           |
| Editor Scheme               | Syntax highlighting and editor colors                                                                          |
| VCS Colors                  | Custom file status colors for version control                                                                  |
| Keymaps                     | Custom keyboard shortcuts                                                                                      |
| Code Style                  | Formatting and indentation settings for multiple languages                                                     |
| Window Layout               | Tool window arrangement and positions                                                                          |
| Selection Highlighting      | Highlights all occurrences of selected text (disabled by default — enable in **Settings > Tools > DDC Theme**) |
| Install/Update Notification | Shows what's new on first install or after an update                                                           |

# Installation

## From Marketplace

1. In your JetBrains IDE, go to **Settings > Plugins > Marketplace**
2. Search for **DDC Theme**
3. Click **Install** and restart the IDE

## From Plugin ZIP

1. Download the latest `DDC-Theme-*.zip` from [Releases](https://github.com/ddc/JetbrainsTheme/releases)
2. Go to **Settings > Plugins > Install Plugin from Disk...**
3. Select the downloaded `.zip` file and restart the IDE

# Getting Started

After install and restart, the **UI Theme**, **Editor Theme**, and **Keymaps** are applied automatically.
The following extras are installed but not activated — enable them if you'd like:

| Extra                      | How to activate                                                |
|----------------------------|----------------------------------------------------------------|
| **Window Layout**          | **Window > Layouts > DDC Window Layout > Restore**             |
| **Code Style**             | **Settings > Editor > Code Style** > select **DDC Code Style** |
| **Selection Highlighting** | **Settings > Tools > DDC Theme** > enable the checkbox         |

> **Note:** All settings are removed automatically when the plugin is uninstalled.

# Building

Requires JDK 21.

```bash
./build.sh                  # build only
./build.sh [-v|--verify]    # build with plugin verification
```

The script formats Kotlin sources (if `ktlint` is available) and builds `DDC-Theme-<version>.zip` inside the
`build/` directory.
Plugin settings and variables are configured at the top of `build.sh`.

# Keymaps Changes

Based on the default keymap with additional shortcuts (defaults are kept):

| Action               | Default      | Added Shortcut |
|----------------------|--------------|----------------|
| Delete Line          | `Ctrl+Y`     | `Ctrl+D`       |
| Rename...            | `Shift+F6`   | `F2`           |
| Reload All from Disk | `Ctrl+Alt+Y` | `F5`           |

The following default shortcuts are removed to avoid conflicts:

| Action                    | Removed Shortcut |
|---------------------------|------------------|
| Duplicate Line            | `Ctrl+D`         |
| Compare Files             | `Ctrl+D`         |
| Show Diff                 | `Ctrl+D`         |
| Send EOF                  | `Ctrl+D`         |
| Go to Desktop             | `Ctrl+D`         |
| Next Highlighted Error    | `F2`             |
| Start Editing (Table)     | `F2`             |
| Start Editing (Tree)      | `F2`             |
| Set Value (Debugger)      | `F2`             |
| Reword Commit             | `F2`, `Shift+F6` |
| Rename Local Branch       | `F2`, `Shift+F6` |
| Edit Changelist           | `F2`, `Shift+F6` |
| Rename Shelved Changelist | `F2`, `Shift+F6` |
| Rename Bookmark           | `F2`             |
| Edit Arrangement Rule     | `F2`             |
| Copy Element              | `F5`             |
| Route Edges (Graph)       | `F5`             |

# Version Control File Status Colors

<table>
<tr>
<td>

| Status                                  | Color                                                | Hex      |
|-----------------------------------------|------------------------------------------------------|----------|
| Added                                   | ![#629755](https://placehold.co/12x12/629755/629755) | `629755` |
| Added (inactive changelist)             | ![#629755](https://placehold.co/12x12/629755/629755) | `629755` |
| Changelist conflict                     | ![#CF84CF](https://placehold.co/12x12/CF84CF/CF84CF) | `CF84CF` |
| Deleted                                 | ![#DE6A66](https://placehold.co/12x12/DE6A66/DE6A66) | `DE6A66` |
| Deleted from file system                | ![#DE6A66](https://placehold.co/12x12/DE6A66/DE6A66) | `DE6A66` |
| Have changed descendants                | ![#FEDB89](https://placehold.co/12x12/FEDB89/FEDB89) | `FEDB89` |
| Have immediate changed children         | ![#FEDB89](https://placehold.co/12x12/FEDB89/FEDB89) | `FEDB89` |
| Hijacked                                | ![#4C72E8](https://placehold.co/12x12/4C72E8/4C72E8) | `4C72E8` |
| Ignored                                 | ![#6F737A](https://placehold.co/12x12/6F737A/6F737A) | `6F737A` |
| Ignored (.ignore plugin)                | ![#6F737A](https://placehold.co/12x12/6F737A/6F737A) | `6F737A` |
| Merged                                  | ![#FEDB89](https://placehold.co/12x12/FEDB89/FEDB89) | `FEDB89` |
| Merged with conflicts                   | ![#CF84CF](https://placehold.co/12x12/CF84CF/CF84CF) | `CF84CF` |
| Merged with property conflicts          | ![#CF84CF](https://placehold.co/12x12/CF84CF/CF84CF) | `CF84CF` |
| Merged with text and property conflicts | ![#CF84CF](https://placehold.co/12x12/CF84CF/CF84CF) | `CF84CF` |
| Modified                                | ![#FEDB89](https://placehold.co/12x12/FEDB89/FEDB89) | `FEDB89` |
| Modified (inactive changelist)          | ![#FEDB89](https://placehold.co/12x12/FEDB89/FEDB89) | `FEDB89` |
| Obsolete                                | ![#6F737A](https://placehold.co/12x12/6F737A/6F737A) | `6F737A` |
| Suppressed                              | ![#6F737A](https://placehold.co/12x12/6F737A/6F737A) | `6F737A` |
| Switched                                | ![#D1D3D9](https://placehold.co/12x12/D1D3D9/D1D3D9) | `D1D3D9` |
| Unknown                                 | ![#D69A6B](https://placehold.co/12x12/D69A6B/D69A6B) | `D69A6B` |
| Up to date                              | ![#D1D3D9](https://placehold.co/12x12/D1D3D9/D1D3D9) | `D1D3D9` |

</td>
<td>
  <img src="assets/example_vcs.png" alt="VCS Colors">
</td>
</tr>
</table>

# License

This project is licensed under the [Apache 2.0 License](LICENSE).

# Support

If you find this project helpful, consider supporting development.

<a href='https://github.com/sponsors/ddc' target='_blank'><img height='24' style='border:0px;height:24px;' src='https://img.shields.io/badge/Sponsor-❤-ea4aaa?style=plastic&logo=github&logoColor=white' border='0' alt='Sponsor on GitHub' /></a>
<a href='https://ko-fi.com/ddc' target='_blank'><img height='30' style='border:0px;height:30px;' src='https://storage.ko-fi.com/cdn/kofi2.png?v=6' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>
<a href='https://www.paypal.com/ncp/payment/6G9Z78QHUD4RJ' target='_blank'><img height='30' style='border:0px;height:30px;' src='https://www.paypalobjects.com/digitalassets/c/website/marketing/apac/C2/logos-buttons/optimize/44_Yellow_PayPal_Pill_Button.png' border='0' alt='Donate via PayPal' /></a>
