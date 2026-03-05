#!/usr/bin/env bash
# Build DDC Theme plugin
# https://plugins.jetbrains.com/plugin/30414-ddc-theme

set -euo pipefail

# ============================================================================
# Plugin Settings — edit these and run the script to update the plugin
# ============================================================================
VERSION="1.0.7"
MIN_PLATFORM_VERSION="2025.3.3"
WHATS_NEW=$(cat <<'EOF'
<ul>
<li>Window Layout available - Window > Layouts > DDC Window Layout</li>
<li>Code Style available - Settings > Editor > Code Style > DDC Code Style</li>
<li>Text selection highlighting - all matching text is highlighted when you select a word (disabled by default)</li>
</ul>

EOF
)
# ============================================================================
JAVA_HOME="$HOME/Programs/java/jdk-21"
GRADLE_BUILD_JVM_ARGS="-Xmx2g"
# ============================================================================
cd "$(dirname "${BASH_SOURCE[0]}")"
export JAVA_HOME

# Derive since-build from platform version (e.g. 2025.3.3 → 253)
SINCE_BUILD="$(echo "$MIN_PLATFORM_VERSION" | sed -E 's/^20([0-9]{2})\.([0-9]+).*/\1\2/')"

# Write settings into gradle.properties so Gradle picks them up
sed -i "s/^pluginVersion = .*/pluginVersion = ${VERSION}/" gradle.properties
sed -i "s/^pluginSinceBuild = .*/pluginSinceBuild = ${SINCE_BUILD}/" gradle.properties
sed -i "s/^platformVersion = .*/platformVersion = ${MIN_PLATFORM_VERSION}/" gradle.properties
sed -i "s/^org.gradle.jvmargs = .*/org.gradle.jvmargs = ${GRADLE_BUILD_JVM_ARGS}/" gradle.properties

# Write change notes for Gradle to inject into plugin.xml and in-IDE notification
echo "$WHATS_NEW" > .whats_new.html

./gradlew buildPlugin "$@"

# Keep only the final zip in build directory
ZIP_FILE="DDC-Theme-${VERSION}.zip"
mv "build/distributions/${ZIP_FILE}" build
rm -rf build/classes build/distributions build/generated build/instrumented \
       build/libs build/reports build/resources build/tmp build/kotlin \
       build/idea-sandbox .gradle .intellijPlatform .kotlin .whats_new.html \
       gradlew.bat
# ============================================================================
echo
echo -e "\033[1;92m✔\033[0m Plugin: build/${ZIP_FILE}"
echo
# ============================================================================
