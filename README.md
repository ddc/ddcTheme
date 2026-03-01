<h1 align="center">
  <img src="https://raw.githubusercontent.com/ddc/ddcTheme/refs/heads/master/assets/ddcTheme-icon.svg" alt="ddcTheme" width="150">
  <br>
  DDC Theme
</h1>

<p align="center">
    <a href="https://www.paypal.com/ncp/payment/6G9Z78QHUD4RJ"><img src="https://img.shields.io/badge/Donate-PayPal-brightgreen.svg?style=plastic&logo=paypal&logoColor=3776AB" alt="Donate"/></a>
    <a href="https://github.com/sponsors/ddc"><img src="https://img.shields.io/static/v1?style=plastic&label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=ff69b4" alt="Sponsor"/></a>
    <br>
    <a href="https://plugins.jetbrains.com/plugin/30414-ddc-theme"><img src="https://img.shields.io/jetbrains/plugin/d/30414?style=plastic&logo=jetbrains&logoColor=white" alt="Marketplace Downloads"/></a>
    <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=plastic&logo=apache&logoColor=white" alt="License: Apache 2.0"/></a>
    <a href="https://github.com/ddc/ddcTheme/releases/latest"><img src="https://img.shields.io/github/v/release/ddc/ddcTheme?style=plastic&logo=github&logoColor=white" alt="Release"/></a>
    <br>
    <a href="https://github.com/ddc/ddcTheme/issues"><img src="https://img.shields.io/github/issues/ddc/ddcTheme?style=plastic&logo=github&logoColor=white" alt="issues"/></a>
    <a href="https://github.com/ddc/ddcTheme/actions/workflows/workflow.yml"><img src="https://img.shields.io/github/actions/workflow/status/ddc/ddcTheme/workflow.yml?style=plastic&logo=github&logoColor=white&label=CI%2FCD%20Pipeline" alt="CI/CD Pipeline"/></a>
    <a href="https://actions-badge.atrox.dev/ddc/ddcTheme/goto?ref=main"><img src="https://img.shields.io/endpoint.svg?url=https%3A//actions-badge.atrox.dev/ddc/ddcTheme/badge?ref=main&label=build&logo=github&style=plastic" alt="Build Status"/></a>
</p>

<p align="center">A dark theme for JetBrains IDEs based on <a href="https://github.com/atom/atom/tree/master/packages/one-dark-ui">Atom One Dark</a> colors.<br>Includes UI Theme, Editor Theme, VCS Colors, and Key Maps.</p>

<p align="center">📦 <b><a href="https://plugins.jetbrains.com/plugin/30414-ddc-theme">Install from JetBrains Marketplace</a></b> 📦 </p>


# Table of Contents
- [Screenshot](#screenshot)
- [Features](#features)
- [Installation](#installation)
    - [From Marketplace](#from-marketplace)
    - [From Plugin JAR](#from-plugin-jar)
    - [Manual Installation](#manual-installation)
- [Building](#building)
- [Version Control File Status Colors](#version-control-file-status-colors)
- [Support](#support)
- [License](#license)


# Screenshot
<p align="left">
  <img src="assets/example_bash.png" alt="Editor Theme">
</p>


# Features
| Component     | File                    | Description                                               |
|---------------|-------------------------|-----------------------------------------------------------|
| UI Theme      | `DDC_Theme.json`        | Dark UI with custom backgrounds, borders, and popups      |
| Editor Scheme | `DDC_Editor_Theme.icls` | Syntax highlighting and editor colors                     |
| VCS Colors    | `DDC_Editor_Theme.icls` | Custom file status colors for version control             |
| Code Style    | `DDC_Code_Style.xml`    | Formatting and indentation settings (manual install only) |
| Key Maps      | `DDC_Key_Maps.xml`      | Custom keyboard shortcuts                                 |


# Installation
## From Marketplace
1. In your JetBrains IDE, go to **Settings > Plugins > Marketplace**
2. Search for **DDC Theme**
3. Click **Install** and restart the IDE

## From Plugin JAR
1. Download the latest `DDC_Theme_*.jar` from [Releases](https://github.com/ddc/ddcTheme/releases)
2. Go to **Settings > Plugins > Install Plugin from Disk...**
3. Select the downloaded `.jar` file and restart the IDE

## Manual Installation
Copy individual files to your JetBrains config directory:

| File                    | Destination          |
|-------------------------|----------------------|
| `DDC_Editor_Theme.icls` | `config/colors/`     |
| `DDC_Code_Style.xml`    | `config/codestyles/` |
| `DDC_Key_Maps.xml`      | `config/keymaps/`    |

> **Note:
** After installing the plugin (Marketplace or JAR), the UI theme, editor scheme, and keymap are included. Code Style is
**not** bundled in the plugin and must be installed manually by copying `DDC_Code_Style.xml` to `config/codestyles/`.


# Building
```bash
./build.sh
```

The script builds `DDC_Theme_<version>.jar` inside the `build/` directory.


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
| Unknown                                 | ![#9A8447](https://placehold.co/12x12/9A8447/9A8447) | `9A8447` |
| Up to date                              | ![#D1D3D9](https://placehold.co/12x12/D1D3D9/D1D3D9) | `D1D3D9` |

</td>
<td>
  <img src="assets/example_vcs.png" alt="VCS Colors">
</td>
</tr>
</table>


# Support
If you find this project helpful, consider supporting development:
- [GitHub Sponsor](https://github.com/sponsors/ddc)
- [ko-fi](https://ko-fi.com/ddcsta)
- [PayPal](https://www.paypal.com/ncp/payment/6G9Z78QHUD4RJ)


# License
Released under the [Apache 2.0](LICENSE)
