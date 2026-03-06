package com.ddc.theme.listeners

import com.ddc.theme.settings.DdcThemeSettings
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.ide.ui.LafManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.psi.codeStyle.CodeStyleSchemes
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemesImpl
import com.intellij.toolWindow.ToolWindowDefaultLayoutManager
import java.nio.file.Files
import java.nio.file.Path

class PluginCleanupListener : PluginStateListener {
    companion object {
        private const val PLUGIN_ID = "com.ddc.theme"
        private const val THEME_NAME = "DDC Theme"
        private const val KEYMAP_NAME = "DDC Key Maps"
        private const val EDITOR_SCHEME_NAME = "DDC Editor Theme"
        private const val LAST_VERSION_KEY = "ddc.theme.lastNotifiedVersion"
        private const val CODE_STYLE_FILE = "DDC_Code_Style.xml"
        private const val CODE_STYLE_SCHEME_NAME = "DDC Code Style"
        private const val WINDOW_LAYOUT_NAME = "DDC Window Layout"
    }

    override fun install(descriptor: IdeaPluginDescriptor) {}

    override fun uninstall(descriptor: IdeaPluginDescriptor) {
        if (descriptor.pluginId.idString != PLUGIN_ID) return
        resetUiTheme()
        resetEditorScheme()
        resetKeymapToDefault()
        removeEditorSchemeFiles()
        removeCodeStyle()
        removeWindowLayout()
        PropertiesComponent.getInstance().unsetValue(LAST_VERSION_KEY)
        try {
            DdcThemeSettings.getInstance().loadState(DdcThemeSettings.State())
        } catch (_: Exception) {
        }
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
                    .filter { it.fileName.toString().contains(EDITOR_SCHEME_NAME) }
                    .forEach { Files.deleteIfExists(it) }
            }
        } catch (_: Exception) {
        }
    }

    private fun removeCodeStyle() {
        try {
            val schemes = CodeStyleSchemes.getInstance()
            if (schemes.currentScheme.name == CODE_STYLE_SCHEME_NAME) {
                schemes.currentScheme = schemes.defaultScheme
            }
            val targetFile = Path.of(PathManager.getConfigPath(), "codestyles", CODE_STYLE_FILE)
            Files.deleteIfExists(targetFile)
            CodeStyleSchemesImpl.getSchemeManager().reload()
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
