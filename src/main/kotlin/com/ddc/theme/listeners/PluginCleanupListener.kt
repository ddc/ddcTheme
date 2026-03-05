package com.ddc.theme.listeners

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.util.PropertiesComponent
import com.ddc.theme.settings.DdcThemeSettings

class PluginCleanupListener : DynamicPluginListener {

    companion object {
        private const val PLUGIN_ID = "com.ddc.theme"
        private const val LAST_VERSION_KEY = "ddc.theme.lastNotifiedVersion"
        private const val LAST_MOD_TIME_KEY = "ddc.theme.lastPluginModTime"
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId.idString != PLUGIN_ID) return
        val properties = PropertiesComponent.getInstance()
        properties.unsetValue(LAST_VERSION_KEY)
        properties.unsetValue(LAST_MOD_TIME_KEY)
        try {
            DdcThemeSettings.getInstance().loadState(DdcThemeSettings.State())
        } catch (_: Exception) {
        }
    }
}