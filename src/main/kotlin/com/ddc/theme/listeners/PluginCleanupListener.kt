package com.ddc.theme.listeners

import com.ddc.theme.settings.DdcThemeSettings
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.ide.plugins.PluginStateManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.toolWindow.ToolWindowDefaultLayoutManager
import java.nio.file.Files
import java.nio.file.Path

class PluginCleanupListener : ProjectActivity {
    companion object {
        private const val PLUGIN_ID = "com.ddc.theme"
        private const val KEYMAP_NAME = "DDC Key Maps"
        private const val LAST_VERSION_KEY = "ddc.theme.lastNotifiedVersion"
        private const val CODE_STYLE_FILE = "DDC_Code_Style.xml"
        private const val WINDOW_LAYOUT_NAME = "DDC Window Layout"

        @Volatile
        private var listenerRegistered = false
    }

    override suspend fun execute(project: Project) {
        if (listenerRegistered) return
        listenerRegistered = true

        PluginStateManager.addStateListener(
            object : PluginStateListener {
                override fun install(descriptor: IdeaPluginDescriptor) {}

                override fun uninstall(descriptor: IdeaPluginDescriptor) {
                    if (descriptor.pluginId.idString != PLUGIN_ID) return
                    resetKeymapToDefault()
                    removeCodeStyle()
                    removeWindowLayout()
                    PropertiesComponent.getInstance().unsetValue(LAST_VERSION_KEY)
                    try {
                        DdcThemeSettings.getInstance().loadState(DdcThemeSettings.State())
                    } catch (_: Exception) {
                    }
                }
            },
        )
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

    private fun removeCodeStyle() {
        try {
            val targetFile = Path.of(PathManager.getConfigPath(), "codestyles", CODE_STYLE_FILE)
            Files.deleteIfExists(targetFile)
        } catch (_: Exception) {
        }
    }

    private fun removeWindowLayout() {
        try {
            val layoutManager = ToolWindowDefaultLayoutManager.getInstance()
            if (WINDOW_LAYOUT_NAME !in layoutManager.getLayoutNames()) return
            if (layoutManager.activeLayoutName == WINDOW_LAYOUT_NAME) {
                layoutManager.activeLayoutName = ""
            }
            layoutManager.deleteLayout(WINDOW_LAYOUT_NAME)
        } catch (_: Exception) {
        }
    }
}
