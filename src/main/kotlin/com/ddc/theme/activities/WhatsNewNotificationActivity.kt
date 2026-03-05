package com.ddc.theme.activities

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.ui.LafManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.ddc.theme.settings.DdcThemeSettings
import java.nio.file.Files

class WhatsNewNotificationActivity : ProjectActivity {

    companion object {
        private const val PLUGIN_ID = "com.ddc.theme"
        private const val LAST_VERSION_KEY = "ddc.theme.lastNotifiedVersion"
        private const val NOTIFICATION_GROUP_ID = "DDC Theme Notifications"
        private const val THEME_NAME = "DDC Theme"
        private const val EDITOR_SCHEME_NAME = "DDC Editor Theme"
        private const val KEYMAP_NAME = "DDC Key Maps"
        @Volatile
        private var processedThisSession = false
    }

    override suspend fun execute(project: Project) {
        if (processedThisSession) return
        val plugin = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID)) ?: return
        val currentVersion = plugin.version
        val pluginDir = plugin.pluginPath.let { if (Files.isDirectory(it)) it else it.parent }
        val markerFile = pluginDir.resolve(".initialized")

        if (!Files.exists(markerFile)) {
            processedThisSession = true
            val properties = PropertiesComponent.getInstance()
            val lastVersion = properties.getValue(LAST_VERSION_KEY)
            val isReinstall = lastVersion == currentVersion

            // Reset settings on reinstall (same version, but plugin dir was recreated)
            if (isReinstall) {
                try {
                    DdcThemeSettings.getInstance().loadState(DdcThemeSettings.State())
                } catch (_: Exception) {
                }
            }

            // Show notification
            val title = "DDC Theme Installed — v$currentVersion"
            val changeNotes = plugin.changeNotes?.trim()
            val content = if (!changeNotes.isNullOrBlank()) {
                changeNotes
            } else {
                "Plugin installed successfully."
            }
            NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(title, content, NotificationType.INFORMATION)
                .notify(project)

            // Auto-apply theme, editor scheme, and keymap on install/reinstall
            ApplicationManager.getApplication().invokeLater {
                activateTheme()
                activateEditorScheme()
                activateKeymap()
            }

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
}