package com.ddc.theme.listeners

import com.ddc.theme.settings.DdcThemeSettings
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemesImpl
import com.intellij.toolWindow.ToolWindowDefaultLayoutManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class DdcThemeInitializer : ProjectActivity {
    companion object {
        private const val PLUGIN_ID = "com.ddc.theme"
        private const val THEME_ID = "com.ddc.theme"
        private const val LAST_VERSION_KEY = "ddc.theme.lastNotifiedVersion"
        private const val NOTIFICATION_GROUP_ID = "DDC Theme Notifications"
        private const val THEME_NAME = "DDC Theme"
        private const val EDITOR_SCHEME_NAME = "DDC Editor Theme"
        private const val KEYMAP_NAME = "DDC Key Maps"
        private const val CODE_STYLE_FILE = "DDC_Code_Style.xml"
        private const val WINDOW_LAYOUT_NAME = "DDC Window Layout"
        private const val LAYOUT_CONFIG_FILE = "window.layouts.xml"

        @Volatile
        private var initialized = false
    }

    override suspend fun execute(project: Project) {
        if (initialized) return
        initialized = true

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

            installCodeStyle()

            val optionsPath = PathManager.getOptionsPath()
            val windowLayoutData = readWindowLayoutResource()

            val configWriter = Thread {
                try { writeThemeConfig(optionsPath) } catch (_: Exception) { }
                try { writeEditorSchemeConfig(optionsPath) } catch (_: Exception) { }
                try { writeKeymapConfig(optionsPath) } catch (_: Exception) { }
                try { writeWindowLayoutConfig(optionsPath, windowLayoutData) } catch (_: Exception) { }
            }
            configWriter.name = "DDC Theme Config Writer"
            Runtime.getRuntime().addShutdownHook(configWriter)

            properties.setValue(LAST_VERSION_KEY, currentVersion)
            try {
                Files.writeString(markerFile, currentVersion)
            } catch (_: Exception) {
            }

            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().restart()
            }
        }
    }

    private fun writeThemeConfig(optionsPath: String) {
        val configFile = Path.of(optionsPath, "laf.xml")
        if (Files.exists(configFile)) {
            val xml = Files.readString(configFile)
            val updated = xml.replace(Regex("""themeId="[^"]*""""), """themeId="$THEME_ID"""")
            Files.writeString(configFile, updated)
        } else {
            val xml = "<application>\n  <component name=\"LafManager\">\n    <laf themeId=\"$THEME_ID\" />\n  </component>\n</application>\n"
            Files.createDirectories(configFile.parent)
            Files.writeString(configFile, xml)
        }
    }

    private fun writeEditorSchemeConfig(optionsPath: String) {
        val configFile = Path.of(optionsPath, "colors.scheme.xml")
        if (Files.exists(configFile)) {
            val xml = Files.readString(configFile)
            val updated = xml.replace(Regex("""<global_color_scheme\s+name="[^"]*"\s*/>"""), """<global_color_scheme name="$EDITOR_SCHEME_NAME" />""")
            Files.writeString(configFile, updated)
        } else {
            val xml = "<application>\n  <component name=\"EditorColorsManagerImpl\">\n    <global_color_scheme name=\"$EDITOR_SCHEME_NAME\" />\n  </component>\n</application>\n"
            Files.createDirectories(configFile.parent)
            Files.writeString(configFile, xml)
        }
    }

    private fun writeKeymapConfig(optionsPath: String) {
        val osDir = when {
            System.getProperty("os.name").lowercase().contains("linux") -> "linux"
            System.getProperty("os.name").lowercase().let { it.contains("mac") || it.contains("darwin") } -> "mac"
            else -> "windows"
        }
        val configFile = Path.of(optionsPath, osDir, "keymap.xml")
        if (Files.exists(configFile)) {
            val xml = Files.readString(configFile)
            val updated = xml.replace(Regex("""<active_keymap\s+name="[^"]*"\s*/>"""), """<active_keymap name="$KEYMAP_NAME" />""")
            Files.writeString(configFile, updated)
        } else {
            val xml = "<application>\n  <component name=\"KeymapManager\">\n    <active_keymap name=\"$KEYMAP_NAME\" />\n  </component>\n</application>\n"
            Files.createDirectories(configFile.parent)
            Files.writeString(configFile, xml)
        }
    }

    private fun readWindowLayoutResource(): String? {
        return try {
            val resource = javaClass.getResourceAsStream("/extras/DDC_Window_Layout.xml") ?: return null
            resource.use { it.bufferedReader().readText() }
        } catch (_: Exception) {
            null
        }
    }

    private fun writeWindowLayoutConfig(optionsPath: String, ddcXmlData: String?) {
        if (ddcXmlData == null) return
        val layoutManager = ToolWindowDefaultLayoutManager.getInstance()
        if (WINDOW_LAYOUT_NAME in layoutManager.getLayoutNames()) return

        val ddcJsonStr = extractJsonFromXml(ddcXmlData) ?: return
        val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
        val ddcState = json.parseToJsonElement(ddcJsonStr).jsonObject
        val ddcLayouts = ddcState["layouts"]?.jsonObject ?: return
        val ddcLayoutEntry = ddcLayouts[WINDOW_LAYOUT_NAME] ?: return

        val configFile = Path.of(optionsPath, LAYOUT_CONFIG_FILE)
        val existingState = if (Files.exists(configFile)) {
            val existingXml = Files.readString(configFile)
            val existingJsonStr = extractJsonFromXml(existingXml)
            if (existingJsonStr != null) {
                json.parseToJsonElement(existingJsonStr).jsonObject
            } else {
                JsonObject(emptyMap())
            }
        } else {
            JsonObject(emptyMap())
        }

        val existingLayouts = existingState["layouts"]?.jsonObject?.toMutableMap() ?: mutableMapOf()
        existingLayouts[WINDOW_LAYOUT_NAME] = ddcLayoutEntry

        val newState = buildJsonObject {
            existingState.forEach { (key, value) ->
                if (key != "layouts") put(key, value)
            }
            put("layouts", JsonObject(existingLayouts))
        }

        val mergedJson = json.encodeToString(JsonElement.serializer(), newState)
        val configXml = "<application>\n  <component name=\"ToolWindowLayout\"><![CDATA[$mergedJson]]></component>\n</application>\n"
        Files.createDirectories(configFile.parent)
        Files.writeString(configFile, configXml)
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

    private fun extractJsonFromXml(xml: String): String? {
        val start = xml.indexOf("<![CDATA[")
        val end = xml.indexOf("]]>")
        if (start < 0 || end < 0) return null
        return xml.substring(start + 9, end).trim()
    }
}
