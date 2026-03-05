package com.ddc.theme.listeners

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.ide.plugins.PluginStateManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.ddc.theme.settings.DdcThemeSettings

class PluginCleanupListener : ProjectActivity {

    companion object {
        private const val PLUGIN_ID = "com.ddc.theme"
        private const val KEYMAP_NAME = "DDC Key Maps"
        private const val LAST_VERSION_KEY = "ddc.theme.lastNotifiedVersion"
        @Volatile
        private var listenerRegistered = false
    }

    override suspend fun execute(project: Project) {
        if (listenerRegistered) return
        listenerRegistered = true

        PluginStateManager.addStateListener(object : PluginStateListener {
            override fun install(descriptor: IdeaPluginDescriptor) {}

            override fun uninstall(descriptor: IdeaPluginDescriptor) {
                if (descriptor.pluginId.idString != PLUGIN_ID) return
                resetKeymapToDefault()
                PropertiesComponent.getInstance().unsetValue(LAST_VERSION_KEY)
                try {
                    DdcThemeSettings.getInstance().loadState(DdcThemeSettings.State())
                } catch (_: Exception) {
                }
            }
        })
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
}
