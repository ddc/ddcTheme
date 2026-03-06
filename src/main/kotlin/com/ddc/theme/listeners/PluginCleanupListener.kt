package com.ddc.theme.listeners

import com.ddc.theme.settings.DdcThemeSettings
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.ide.plugins.PluginStateManager
import com.intellij.ide.ui.LafManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.codeStyle.CodeStyleSchemes
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemesImpl
import com.intellij.toolWindow.ToolWindowDefaultLayoutManager
import java.nio.file.Files
import java.nio.file.Path

object PluginCleanupListener {
    private val LOG = logger<PluginCleanupListener>()
    private const val PLUGIN_ID = "com.ddc.theme"
    private const val THEME_NAME = "DDC Dark"
    private const val KEYMAP_NAME = "DDC_Keymaps"
    private const val EDITOR_SCHEME_NAME = "DDC Editor Dark"
    private const val LAST_VERSION_KEY = "ddc.theme.lastNotifiedVersion"
    private const val CODE_STYLE_SCHEME_NAME = "DDC Code Style"
    private const val WINDOW_LAYOUT_NAME = "DDC Window Layout"

    @Volatile
    private var registered = false

    fun register() {
        if (registered) return
        registered = true

        PluginStateManager.addStateListener(
            object : PluginStateListener {
                override fun install(descriptor: IdeaPluginDescriptor) {}

                override fun uninstall(descriptor: IdeaPluginDescriptor) {
                    LOG.info("DDC: uninstall called for ${descriptor.pluginId}")
                    if (descriptor.pluginId.idString != PLUGIN_ID) return
                    LOG.info("DDC: running cleanup")
                    resetUiTheme()
                    resetEditorScheme()
                    resetKeymapToDefault()
                    removeEditorSchemeFiles()
                    removeCodeStyle()
                    removeWindowLayout()
                    deleteMarkerFile(descriptor)
                    PropertiesComponent.getInstance().unsetValue(LAST_VERSION_KEY)
                    try {
                        DdcThemeSettings.getInstance().loadState(DdcThemeSettings.State())
                    } catch (_: Exception) {
                    }
                    LOG.info("DDC: cleanup complete")
                }
            },
        )
    }

    private fun resetUiTheme() {
        try {
            val lafManager = LafManager.getInstance()
            val current = lafManager.currentUIThemeLookAndFeel ?: return
            if (current.name != THEME_NAME) return
            val fallback = lafManager.defaultDarkLaf ?: return
            lafManager.setCurrentUIThemeLookAndFeel(fallback)
            lafManager.updateUI()
        } catch (_: Exception) {
        }
    }

    private fun resetEditorScheme() {
        try {
            val ecm = EditorColorsManager.getInstance()
            val current = ecm.globalScheme
            if (!current.name.contains(EDITOR_SCHEME_NAME)) return
            val fallback = ecm.allSchemes.firstOrNull { !it.name.contains(EDITOR_SCHEME_NAME) } ?: return
            ecm.setGlobalScheme(fallback)
        } catch (_: Exception) {
        }
    }

    private fun resetKeymapToDefault() {
        try {
            val keymapManager = KeymapManagerEx.getInstanceEx()
            if (keymapManager.activeKeymap.name != KEYMAP_NAME) return
            val defaultKeymap = keymapManager.getKeymap(KeymapManager.DEFAULT_IDEA_KEYMAP) ?: return
            keymapManager.activeKeymap = defaultKeymap
        } catch (_: Exception) {
        }
    }

    private fun removeEditorSchemeFiles() {
        try {
            val colorsDir = Path.of(PathManager.getConfigPath(), "colors")
            if (!Files.isDirectory(colorsDir)) return
            Files.list(colorsDir).use { stream ->
                stream
                    .filter {
                        it.fileName
                            .toString()
                            .lowercase()
                            .contains("ddc")
                    }.forEach { Files.deleteIfExists(it) }
            }
        } catch (_: Exception) {
        }
    }

    private fun removeCodeStyle() {
        // Reset application-level preferred code style
        try {
            val appManager = CodeStyleSettingsManager.getInstance()
            LOG.info("DDC: removeCodeStyle - app preferred=${appManager.PREFERRED_PROJECT_CODE_STYLE}")
            if (appManager.PREFERRED_PROJECT_CODE_STYLE == CODE_STYLE_SCHEME_NAME) {
                appManager.PREFERRED_PROJECT_CODE_STYLE = null
            }
        } catch (e: Exception) {
            LOG.warn("DDC: removeCodeStyle - app reset failed", e)
        }
        // Reset per-project preferred code style (in-memory + config file)
        try {
            for (p in ProjectManager.getInstance().openProjects) {
                val settingsManager = CodeStyleSettingsManager.getInstance(p)
                LOG.info("DDC: removeCodeStyle - project=${p.name} preferred=${settingsManager.PREFERRED_PROJECT_CODE_STYLE}")
                if (settingsManager.PREFERRED_PROJECT_CODE_STYLE == CODE_STYLE_SCHEME_NAME) {
                    settingsManager.PREFERRED_PROJECT_CODE_STYLE = null
                    settingsManager.fireCodeStyleSettingsChanged()
                }
                // Revert the project-level config file we wrote during install
                val projectPath = p.basePath
                if (projectPath != null) {
                    val configFile = Path.of(projectPath, ".idea", "codeStyles", "codeStyleConfig.xml")
                    if (Files.exists(configFile)) {
                        val content = Files.readString(configFile)
                        if (content.contains(CODE_STYLE_SCHEME_NAME)) {
                            Files.writeString(
                                configFile,
                                "<component name=\"ProjectCodeStyleConfiguration\">\n" +
                                    "  <state>\n" +
                                    "    <option name=\"PREFERRED_PROJECT_CODE_STYLE\" value=\"Default\" />\n" +
                                    "  </state>\n" +
                                    "</component>\n",
                            )
                            LOG.info("DDC: removeCodeStyle - reverted codeStyleConfig.xml for ${p.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOG.warn("DDC: removeCodeStyle - project reset failed", e)
        }
        // Remove scheme via SchemeManager directly (not CodeStyleSchemes.deleteScheme())
        // SchemeManager.removeScheme() properly cleans internal state so auto-save won't recreate the file
        try {
            val schemeManager = CodeStyleSchemesImpl.getSchemeManager()
            val schemes = CodeStyleSchemes.getInstance()
            val ddcScheme = schemeManager.findSchemeByName(CODE_STYLE_SCHEME_NAME)
            LOG.info("DDC: removeCodeStyle - schemeManager scheme found: ${ddcScheme != null}")
            if (ddcScheme != null) {
                if (schemes.currentScheme == ddcScheme) {
                    schemes.currentScheme = schemes.defaultScheme
                }
                schemeManager.removeScheme(CODE_STYLE_SCHEME_NAME)
                LOG.info("DDC: removeCodeStyle - removed from SchemeManager by name")
            }
            schemeManager.save()
            LOG.info("DDC: removeCodeStyle - SchemeManager.save() called")
        } catch (e: Exception) {
            LOG.warn("DDC: removeCodeStyle - scheme remove failed", e)
        }
        // Also delete files directly as a safety net
        val configPathStr = PathManager.getConfigPath()
        deleteDdcCodeStyleFiles(configPathStr)
    }

    private fun deleteMarkerFile(descriptor: IdeaPluginDescriptor) {
        try {
            val pluginDir = descriptor.pluginPath.let { if (Files.isDirectory(it)) it else it.parent }
            val markerFile = pluginDir.resolve(".initialized")
            Files.deleteIfExists(markerFile)
        } catch (_: Exception) {
        }
    }

    private fun deleteDdcCodeStyleFiles(configPathStr: String) {
        // Uses only java.nio.file APIs — safe for JVM shutdown hooks where classloaders may be gone
        try {
            val codestyles =
                java.nio.file.Paths
                    .get(configPathStr, "codestyles")
            if (Files.isDirectory(codestyles)) {
                Files.list(codestyles).use { stream ->
                    stream
                        .filter {
                            it.fileName
                                .toString()
                                .lowercase()
                                .contains("ddc")
                        }.forEach { Files.deleteIfExists(it) }
                }
            }
        } catch (_: Exception) {
        }
        try {
            val syncCodestyles =
                java.nio.file.Paths
                    .get(configPathStr, "settingsSync", "codestyles")
            if (Files.isDirectory(syncCodestyles)) {
                Files.list(syncCodestyles).use { stream ->
                    stream
                        .filter {
                            it.fileName
                                .toString()
                                .lowercase()
                                .contains("ddc")
                        }.forEach { Files.deleteIfExists(it) }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun removeWindowLayout() {
        try {
            val layoutManager = ToolWindowDefaultLayoutManager.getInstance()
            val layouts = layoutManager.getLayoutNames()
            LOG.info("DDC: removeWindowLayout - layouts=$layouts, active=${layoutManager.activeLayoutName}")
            if (WINDOW_LAYOUT_NAME !in layouts) return
            if (layoutManager.activeLayoutName == WINDOW_LAYOUT_NAME) {
                layoutManager.activeLayoutName = ""
            }
            layoutManager.deleteLayout(WINDOW_LAYOUT_NAME)
            LOG.info("DDC: removeWindowLayout - deleted from memory, layouts now=${layoutManager.getLayoutNames()}")
        } catch (e: Exception) {
            LOG.warn("DDC: removeWindowLayout failed", e)
        }
        // Force immediate save so IDE save-on-shutdown sees no changes needed
        try {
            ApplicationManager.getApplication().saveSettings()
            LOG.info("DDC: removeWindowLayout - forced saveSettings()")
        } catch (e: Exception) {
            LOG.warn("DDC: removeWindowLayout - saveSettings failed", e)
        }
        // Also clean config files directly as safety net
        removeWindowLayoutFromConfigFiles()
    }

    private fun removeWindowLayoutFromConfigFiles() {
        val configPath = PathManager.getConfigPath()
        val files =
            listOf(
                Path.of(configPath, "options", "window.layouts.xml"),
                Path.of(configPath, "settingsSync", "options", "window.layouts.xml"),
            )
        for (file in files) {
            try {
                if (!Files.exists(file)) continue
                val content = Files.readString(file)
                if (!content.contains(WINDOW_LAYOUT_NAME)) continue
                // Remove the "DDC Window Layout": { ... } entry from the JSON inside CDATA
                // Use regex to match the key and its entire JSON object value
                val pattern = Regex(""""DDC Window Layout"\s*:\s*\{""")
                val match = pattern.find(content) ?: continue
                val keyStart = match.range.first
                // Find the matching closing brace by counting braces
                var braceCount = 0
                var objectEnd = -1
                for (i in match.range.last..content.lastIndex) {
                    when (content[i]) {
                        '{' -> {
                            braceCount++
                        }

                        '}' -> {
                            braceCount--
                            if (braceCount == 0) {
                                objectEnd = i
                                break
                            }
                        }
                    }
                }
                if (objectEnd < 0) continue
                // Also remove trailing comma or leading comma
                var removeStart = keyStart
                var removeEnd = objectEnd + 1
                // Remove leading comma+whitespace if present
                val before = content.substring(0, keyStart).trimEnd()
                if (before.endsWith(",")) {
                    removeStart = before.lastIndexOf(",")
                }
                // Or remove trailing comma if present
                val after = content.substring(removeEnd).trimStart()
                if (after.startsWith(",") && removeStart == keyStart) {
                    removeEnd += content.substring(removeEnd).indexOf(",") + 1
                }
                val newContent = content.substring(0, removeStart) + content.substring(removeEnd)
                Files.writeString(file, newContent)
                LOG.info("DDC: removeWindowLayoutFromConfigFiles - cleaned $file")
            } catch (e: Exception) {
                LOG.warn("DDC: removeWindowLayoutFromConfigFiles - failed for $file", e)
            }
        }
    }
}
