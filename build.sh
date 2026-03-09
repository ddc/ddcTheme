#!/usr/bin/env bash
# ============================================================================
# Automated script to build the plugin and register changes on the changelog file
# DDC Softwares (daniel@ddcsoftwares.com)
# https://plugins.jetbrains.com/plugin/30414-ddc-theme
# ============================================================================
set -euo pipefail


# ============================================================================
# Variables
# ============================================================================
PLUGIN_VERSION="1.0.8"
GRADLE_VERSION="9.4.0"
KOTLIN_VERSION="2.1.0"
INTELLIJ_PLATFORM_VERSION="2.12.0"
MIN_PLUGIN_PLATFORM_VERSION="2025.3.3"
JAVA_HOME="$HOME/Programs/java/jdk-21"
GRADLE_BUILD_JVM_ARGS="-Xmx2g"
OUTPUT_FILENAME="DDC-Theme-${PLUGIN_VERSION}.zip"
# ============================================================================
WHATS_NEW=$(cat <<'EOF'
<ul>
<li>Fixed popup backgrounds for dark theme</li>
</ul>

EOF
)
# ============================================================================
pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null
export JAVA_HOME
# ============================================================================
BLUE="\033[1;94m"
GREEN="\033[1;92m"
NC="\033[0m"
log_action() { echo -e "${BLUE}➜ [ACTION]${NC} $*"; }
log_success() { echo -e "${GREEN}✓ [SUCCESS]${NC} $*"; }
echo


# ============================================================================
# Functions
# ============================================================================
write_gradle_properties() {
    log_action "Writing settings into gradle.properties..."
    local since_build
    since_build="$(echo "$MIN_PLUGIN_PLATFORM_VERSION" | sed -E 's/^20([0-9]{2})\.([0-9]+).*/\1\2/')"
    sed -i "s/^pluginVersion = .*/pluginVersion = ${PLUGIN_VERSION}/" gradle.properties
    sed -i "s/^pluginSinceBuild = .*/pluginSinceBuild = ${since_build}/" gradle.properties
    sed -i "s/^platformVersion = .*/platformVersion = ${MIN_PLUGIN_PLATFORM_VERSION}/" gradle.properties
    sed -i "s/^org.gradle.jvmargs = .*/org.gradle.jvmargs = ${GRADLE_BUILD_JVM_ARGS}/" gradle.properties
    sed -i "s|gradle-[0-9.]*-bin.zip|gradle-${GRADLE_VERSION}-bin.zip|" gradle/wrapper/gradle-wrapper.properties
    sed -i "s|GRADLE_VERSION: \"[0-9.]*\"|GRADLE_VERSION: \"${GRADLE_VERSION}\"|" .github/workflows/workflow.yml
    sed -i "s|^kotlin = \"[0-9.]*\"|kotlin = \"${KOTLIN_VERSION}\"|" gradle/libs.versions.toml
    sed -i "s|^intellijPlatform = \"[0-9.]*\"|intellijPlatform = \"${INTELLIJ_PLATFORM_VERSION}\"|" gradle/libs.versions.toml
    echo "$WHATS_NEW" > .whats_new.html
}

update_changelog() {
    log_action "Updating Changelog..."
    local items after
    items="$(echo "$WHATS_NEW" | sed -n 's|<li>\(.*\)</li>|- \1|p')"
    if [[ -f CHANGELOG.md ]] && grep -q "## v${PLUGIN_VERSION}" CHANGELOG.md; then
        after="$(sed -n "/^## v${PLUGIN_VERSION}$/,\${ /^## v${PLUGIN_VERSION}$/!p; }" CHANGELOG.md | sed -n '/^## v/,$p')"
        { echo "# Changelog"; echo; echo "## v${PLUGIN_VERSION}"; echo "$items"; echo; if [[ -n "$after" ]]; then echo "$after"; fi } > CHANGELOG.tmp
        mv CHANGELOG.tmp CHANGELOG.md
    elif [[ -f CHANGELOG.md ]]; then
        { echo "# Changelog"; echo; echo "## v${PLUGIN_VERSION}"; echo "$items"; echo; tail -n +2 CHANGELOG.md; } > CHANGELOG.tmp
        mv CHANGELOG.tmp CHANGELOG.md
    else
        { echo "# Changelog"; echo; echo "## v${PLUGIN_VERSION}"; echo "$items"; echo; } > CHANGELOG.md
    fi
}

format_kotlin() {
    if command -v ktlint &> /dev/null; then
        log_action "Running ktlint..."
        ktlint --format "src/**/*.kt"
    fi
}

verify_plugin() {
    log_action "Verifying plugin..."
    local output
    output="$(./gradlew verifyPlugin 2>&1)" || { echo "$output"; exit 1; }
    echo "$output" | grep -iE "warning|problem|error|internal|experimental|deprecated|incompatible|Plugin .* against" || true
}

build_plugin() {
    log_action "Building plugin..."
    ./gradlew buildPlugin -q
}

cleanup_build() {
    log_action "Cleaning up build files..."
    mv "build/distributions/${OUTPUT_FILENAME}" build
    rm -rf build/classes build/distributions build/generated build/instrumented \
           build/libs build/reports build/resources build/tmp build/kotlin \
           build/idea-sandbox .gradle .intellijPlatform .kotlin .whats_new.html \
           gradlew.bat
    echo
}


# ============================================================================
# Build
# ============================================================================
write_gradle_properties
update_changelog
format_kotlin
verify_plugin
build_plugin
cleanup_build

log_success "Plugin: ./build/${OUTPUT_FILENAME}"
popd > /dev/null
