package com.ddc.theme.activities

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
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
        private const val LAST_MOD_TIME_KEY = "ddc.theme.lastPluginModTime"
        private const val NOTIFICATION_GROUP_ID = "DDC Theme Notifications"
        private const val KEYMAP_NAME = "DDC Key Maps"
        @Volatile
        private var notifiedThisSession = false
    }

    override suspend fun execute(project: Project) {
        if (notifiedThisSession) return
        val plugin = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID)) ?: return
        val currentVersion = plugin.version
        val properties = PropertiesComponent.getInstance()
        val lastVersion = properties.getValue(LAST_VERSION_KEY)

        val currentModTime = getPluginModTime(plugin.pluginPath)
        val lastModTime = properties.getValue(LAST_MOD_TIME_KEY, "")

        val versionChanged = lastVersion != currentVersion
        val modTimeChanged = currentModTime.isNotEmpty() && currentModTime != lastModTime

        if (versionChanged || modTimeChanged) {
            notifiedThisSession = true

            // Reset settings on reinstall (same version but different installation)
            if (!versionChanged && modTimeChanged) {
                try {
                    DdcThemeSettings.getInstance().loadState(DdcThemeSettings.State())
                } catch (_: Exception) {
                }
            }

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

            properties.setValue(LAST_VERSION_KEY, currentVersion)
            properties.setValue(LAST_MOD_TIME_KEY, currentModTime)
        }

        // Auto-select DDC keymap if not already active
        activateKeymapIfNeeded()
    }

    private fun getPluginModTime(pluginPath: java.nio.file.Path): String {
        return try {
            val targetPath = if (Files.isDirectory(pluginPath)) {
                val libDir = pluginPath.resolve("lib")
                if (Files.isDirectory(libDir)) {
                    Files.list(libDir).use { stream ->
                        stream.filter { it.toString().endsWith(".jar") }
                            .findFirst().orElse(pluginPath)
                    }
                } else pluginPath
            } else {
                pluginPath
            }
            Files.getLastModifiedTime(targetPath).toMillis().toString()
        } catch (_: Exception) {
            ""
        }
    }

    private fun activateKeymapIfNeeded() {
        try {
            val keymapManager = KeymapManagerEx.getInstanceEx()
            if (keymapManager.activeKeymap.name == KEYMAP_NAME) return
            val ddcKeymap = keymapManager.getKeymap(KEYMAP_NAME) ?: return
            keymapManager.activeKeymap = ddcKeymap
        } catch (_: Exception) {
            // Keymap activation may fail if not on EDT; non-critical
        }
    }
}