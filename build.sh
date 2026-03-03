#!/usr/bin/env bash
# Build DDC_Theme.jar plugin.
#
# https://plugins.jetbrains.com/plugin/30414-ddc-theme

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
pushd "$SCRIPT_DIR" || { log_error "Failed to change to script directory" 1>&2; exit 1; }

# ============================================================================
# Plugin Settings — edit these and run the script to update the plugin
# ============================================================================
VERSION="1.0.5"
WHATS_NEW=$(cat <<'EOF'
<li>Renamed plugin name to DDC Theme</li>
<li>Removed identifier under caret highlight</li>
EOF
)
MIN_IDE_VERSION="253"
# ============================================================================
#  DO NOT CHANGE VARIABLES BELLOW
# ============================================================================
TITLE="DDC Theme"
DESCRIPTION="DDC Theme for JetBrains IDEs based on Atom dark colors. Includes UI Theme, Editor Theme, VCS Colors, and Key Maps."
VENDOR_URL="https://github.com/ddc/JetbrainsTheme"
EMAIL="daniel@ddcsoftwares.com"
ID="com.ddc.theme"
# ============================================================================
UI_JSON_THEME_NAME="DDC_Theme.json"
EDITOR_ICLS_THEME_NAME="DDC_Editor_Theme.icls"
KEYMAP_XML_NAME="DDC_Key_Maps.xml"
BUILD_DIR="build"
OUTPUT_PLUGIN_JAR_NAME="DDC_Jetbrains_Theme_v${VERSION}.jar"
WHATS_NEW_PERSISTENT="<li><b>Note:</b> after plugin updates, you may need to reselect the UI Theme, Editor Theme, and Key Maps in Settings</li>"
# ============================================================================
THEME_JSON="$(basename "${UI_JSON_THEME_NAME}" .json | tr '_' ' ').theme.json"
EDITOR_SCHEME="$(basename "${EDITOR_ICLS_THEME_NAME}" .icls | tr '_' ' ')"
KEYMAP="$(basename "${KEYMAP_XML_NAME}" .xml | tr '_' ' ')"
NEW_ICLS="${1:-$EDITOR_ICLS_THEME_NAME}"
# ============================================================================

TMPDIR=$(mktemp -d)
trap 'rm -rf "$TMPDIR"' EXIT

# Verify all source files exist
if [[ ! -f "$UI_JSON_THEME_NAME" ]]; then
    echo "Error: $UI_JSON_THEME_NAME not found"
    exit 1
fi
if [[ ! -f "$NEW_ICLS" ]]; then
    echo "Error: $NEW_ICLS not found"
    exit 1
fi
if [[ ! -f "$KEYMAP_XML_NAME" ]]; then
    echo "Error: $KEYMAP_XML_NAME not found"
    exit 1
fi

# Step 2: Clean and create build dir
#rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

# Step 3: Build new JAR from source files
mkdir -p "$TMPDIR/META-INF" "$TMPDIR/theme" "$TMPDIR/colors" "$TMPDIR/keymaps"

cp "$UI_JSON_THEME_NAME" "$TMPDIR/theme/${THEME_JSON}"
cp "$NEW_ICLS" "$TMPDIR/colors/${EDITOR_SCHEME}.xml"
cp "$KEYMAP_XML_NAME" "$TMPDIR/keymaps/${KEYMAP}.xml"

cat > "$TMPDIR/META-INF/plugin.xml" << EOF
<idea-plugin>
  <id>${ID}</id>
  <name>${TITLE}</name>
  <version>${VERSION}</version>
  <vendor email="${EMAIL}" url="${VENDOR_URL}">DDC</vendor>
  <description><![CDATA[${DESCRIPTION}]]></description>
  <change-notes><![CDATA[<ul>${WHATS_NEW}${WHATS_NEW_PERSISTENT}</ul>]]></change-notes>
  <idea-version since-build="${MIN_IDE_VERSION}"/>
  <depends>com.intellij.modules.platform</depends>
  <extensions defaultExtensionNs="com.intellij">
    <themeProvider id="${ID}" path="/theme/${THEME_JSON}"/>
    <bundledColorScheme path="/colors/${EDITOR_SCHEME}"/>
    <bundledKeymap file="${KEYMAP}.xml"/>
  </extensions>
</idea-plugin>
EOF

(cd "$TMPDIR" && jar cfM "$SCRIPT_DIR/$BUILD_DIR/$OUTPUT_PLUGIN_JAR_NAME" META-INF/ theme/ colors/ keymaps/)
echo "Color scheme: $(basename "$NEW_ICLS")"
echo "Keymap: $(basename "$KEYMAP_XML_NAME")"

# ============================================================================
popd || { log_error "Failed to return to previous directory" 1>&2; exit 1; }
echo
echo "Done. DDC_Theme.jar v${VERSION}"
echo
