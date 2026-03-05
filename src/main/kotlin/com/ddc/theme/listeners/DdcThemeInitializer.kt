package com.ddc.theme.listeners

import com.ddc.theme.settings.DdcThemeSettings
import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.ui.LafManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemesImpl
import com.intellij.toolWindow.ToolWindowDefaultLayoutManager
import com.intellij.toolWindow.ToolWindowLayoutStorageManagerState
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class DdcThemeInitializer : AppLifecycleListener {
    companion object {
        private const val PLUGIN_ID = "com.ddc.theme"
        private const val LAST_VERSION_KEY = "ddc.theme.lastNotifiedVersion"
        private const val NOTIFICATION_GROUP_ID = "DDC Theme Notifications"
        private const val THEME_NAME = "DDC Theme"
        private const val EDITOR_SCHEME_NAME = "DDC Editor Theme"
        private const val KEYMAP_NAME = "DDC Key Maps"
        private const val CODE_STYLE_FILE = "DDC_Code_Style.xml"
        private const val WINDOW_LAYOUT_NAME = "DDC Window Layout"
    }

    override fun appStarted() {
        val plugin = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID)) ?: return
        val currentVersion = plugin.version
        val pluginDir = plugin.pluginPath.let { if (Files.isDirectory(it)) it else it.parent }
        val markerFile = pluginDir.resolve(".initialized")

        if (!Files.exists(markerFile)) {
            val properties = PropertiesComponent.getInstance()
            val lastVersion = properties.getValue(LAST_VERSION_KEY)
            val isReinstall = lastVersion == currentVersion

            if (isReinstall) {
                try {
                    DdcThemeSettings.getInstance().loadState(DdcThemeSettings.State())
                } catch (_: Exception) {
                }
            }

            val title = "DDC Theme Installed — v$currentVersion"
            val changeNotes = plugin.changeNotes?.trim()
            val content =
                if (!changeNotes.isNullOrBlank()) {
                    changeNotes
                } else {
                    "Plugin installed successfully."
                }
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(title, content, NotificationType.INFORMATION)
                .notify(null)

            ApplicationManager.getApplication().invokeLater {
                activateTheme()
                activateEditorScheme()
                activateKeymap()
            }

            installCodeStyle()
            installWindowLayout()

            properties.setValue(LAST_VERSION_KEY, currentVersion)
            try {
                Files.writeString(markerFile, currentVersion)
            } catch (_: Exception) {
            }
        }
    }

    private fun activateTheme() {
        try {
            val lafManager = LafManager.getInstance()
            if (lafManager.currentUIThemeLookAndFeel?.name == THEME_NAME) return
            val ddcTheme = lafManager.installedThemes.find { it.name == THEME_NAME } ?: return
            lafManager.setCurrentLookAndFeel(ddcTheme, false)
            lafManager.updateUI()
        } catch (_: Exception) {
        }
    }

    private fun activateEditorScheme() {
        try {
            val colorsManager = EditorColorsManager.getInstance()
            if (colorsManager.globalScheme.name == EDITOR_SCHEME_NAME) return
            val ddcScheme = colorsManager.allSchemes.find { it.name == EDITOR_SCHEME_NAME } ?: return
            colorsManager.setGlobalScheme(ddcScheme)
        } catch (_: Exception) {
        }
    }

    private fun activateKeymap() {
        try {
            val keymapManager = KeymapManagerEx.getInstanceEx()
            if (keymapManager.activeKeymap.name == KEYMAP_NAME) return
            val ddcKeymap = keymapManager.getKeymap(KEYMAP_NAME) ?: return
            keymapManager.activeKeymap = ddcKeymap
        } catch (_: Exception) {
        }
    }

    private fun installCodeStyle() {
        try {
            val codeStylesDir = Path.of(PathManager.getConfigPath(), "codestyles")
            Files.createDirectories(codeStylesDir)
            val targetFile = codeStylesDir.resolve(CODE_STYLE_FILE)
            if (Files.exists(targetFile)) return
            val resource = javaClass.getResourceAsStream("/extras/$CODE_STYLE_FILE") ?: return
            resource.use { Files.copy(it, targetFile, StandardCopyOption.REPLACE_EXISTING) }
            CodeStyleSchemesImpl.getSchemeManager().reload()
        } catch (_: Exception) {
        }
    }

    private fun installWindowLayout() {
        try {
            val layoutManager = ToolWindowDefaultLayoutManager.getInstance()
            if (WINDOW_LAYOUT_NAME in layoutManager.getLayoutNames()) return

            val resource = javaClass.getResourceAsStream("/extras/DDC_Window_Layout.xml") ?: return
            val ddcXml = resource.use { it.bufferedReader().readText() }
            val ddcJson = extractJsonFromXml(ddcXml) ?: return

            val json = Json { ignoreUnknownKeys = true }
            val ddcState = json.decodeFromString(ToolWindowLayoutStorageManagerState.serializer(), ddcJson)
            val ddcLayoutEntry = ddcState.layouts[WINDOW_LAYOUT_NAME] ?: return

            val currentState = layoutManager.state
            val newLayouts = currentState.layouts.toMutableMap()
            newLayouts[WINDOW_LAYOUT_NAME] = ddcLayoutEntry
            val newState = currentState.copy(layouts = newLayouts)
            layoutManager.loadState(newState)
        } catch (_: Exception) {
        }
    }

    private fun extractJsonFromXml(xml: String): String? {
        val start = xml.indexOf("<![CDATA[")
        val end = xml.indexOf("]]>")
        if (start < 0 || end < 0) return null
        return xml.substring(start + 9, end).trim()
    }
}
