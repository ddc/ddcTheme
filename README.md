<h1 align="center">
  <img src="https://raw.githubusercontent.com/ddc/ddcTheme/refs/heads/master/assets/ddcTheme-icon.svg" alt="ddcTheme" width="150">
  <br>
  DDC Theme
</h1>

<p align="center">
    <a href="https://www.paypal.com/ncp/payment/6G9Z78QHUD4RJ"><img src="https://img.shields.io/badge/Donate-PayPal-brightgreen.svg?style=plastic" alt="Donate"/></a>
    <a href="https://github.com/sponsors/ddc"><img src="https://img.shields.io/static/v1?style=plastic&label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=ff69b4" alt="Sponsor"/></a>
    <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic" alt="License: Apache 2.0"/></a>
    <br>
    <a href="https://github.com/ddc/ddcTheme/actions/workflows/release.yml"><img src="https://github.com/ddc/ddcTheme/actions/workflows/release.yml/badge.svg" alt="Build and Release"/></a>
    <a href="https://github.com/ddc/ddcTheme/releases/latest"><img src="https://img.shields.io/github/v/release/ddc/ddcTheme?style=plastic" alt="Release"/></a>
    <a href="https://plugins.jetbrains.com/plugin/30414-ddc-theme"><img src="https://img.shields.io/jetbrains/plugin/d/30414?style=plastic" alt="Marketplace Downloads"/></a>
</p>

<p align="center">A dark theme for JetBrains IDEs based on Atom dark colors. Includes UI Theme, Editor Theme, VCS Colors, Code Style, and Key Maps.</p>


**[Install from JetBrains Marketplace](https://plugins.jetbrains.com/plugin/30414-ddc-theme)**

## Table of Contents

- [Included Files](#included-files)
- [Installation](#installation)
    - [From Plugin JAR](#from-plugin-jar)
    - [Manual Installation](#manual-installation)
- [Building](#building)
- [VCS Colors](#vcs-colors)

## Included Files

| File                    | Description         |
|-------------------------|---------------------|
| `DDC_Theme.json`        | UI Theme            |
| `DDC_Editor_Theme.icls` | Editor Color Scheme |
| `DDC_Code_Style.xml`    | Code Style settings |
| `DDC_Key_Maps.xml`      | Key Maps            |

## Installation

### From Plugin JAR

1. Download the latest `DDC_Theme_*.jar` from [Releases](https://github.com/ddc/ddcTheme/releases)
2. In your JetBrains IDE, go to **Settings > Plugins > Install Plugin from Disk...**
3. Select the downloaded `.jar` file and restart the IDE

### Manual Installation

Copy individual files to your JetBrains config directory:

- `DDC_Editor_Theme.icls` &rarr; `config/colors/`
- `DDC_Code_Style.xml` &rarr; `config/codestyles/`
- `DDC_Key_Maps.xml` &rarr; `config/keymaps/`

## Building

```bash
./build.sh
```

The script builds `DDC_Theme_<version>.jar` inside the `build/` directory.

## VCS Colors

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
| Unknown                                 | ![#9A8447](https://placehold.co/12x12/9A8447/9A8447) | `9A8447` |
| Up to date                              | ![#D1D3D9](https://placehold.co/12x12/D1D3D9/D1D3D9) | `D1D3D9` |
