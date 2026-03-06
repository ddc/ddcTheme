package com.ddc.theme.listeners

import com.ddc.theme.settings.DdcThemeSettings
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.codeStyle.CodeStyleSchemes
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemesImpl
import com.intellij.toolWindow.ToolWindowDefaultLayoutManager
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class DdcThemeInitializer : ProjectActivity {
    companion object {
        private const val PLUGIN_ID = "com.ddc.theme"
        private const val LAST_VERSION_KEY = "ddc.theme.lastNotifiedVersion"
        private const val NOTIFICATION_GROUP_ID = "DDC Theme Notifications"
        private const val EDITOR_SCHEME_NAME = "DDC Editor Theme"
        private const val KEYMAP_NAME = "DDC Key Maps"
        private const val CODE_STYLE_FILE = "DDC_Code_Style.xml"
        private const val CODE_STYLE_SCHEME_NAME = "DDC Code Style"
        private const val WINDOW_LAYOUT_NAME = "DDC Window Layout"

        @Volatile
        private var initialized = false
    }

    override suspend fun execute(project: Project) {
        if (initialized) return
        initialized = true

        PluginCleanupListener.register()

        val plugin = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID)) ?: return
        val currentVersion = plugin.version
        val pluginDir = plugin.pluginPath.let { if (Files.isDirectory(it)) it else it.parent }
        val markerFile = pluginDir.resolve(".initialized")

        if (Files.exists(markerFile)) return

        val properties = PropertiesComponent.getInstance()
        val lastVersion = properties.getValue(LAST_VERSION_KEY)
        val isReinstall = lastVersion == currentVersion

        if (isReinstall) {
            try {
                DdcThemeSettings.getInstance().loadState(DdcThemeSettings.State())
            } catch (_: Exception) {
            }
        }

        // Window layout uses reflection (safe from any thread)
        installWindowLayout()

        // All other settings must be applied on the EDT.
        // Marker file is written only after they take effect,
        // so a restart will retry if dynamic load didn't apply them.
        ApplicationManager.getApplication().invokeLater({
            applyEditorScheme()
            applyKeymap()
            installAndApplyCodeStyle()

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

            properties.setValue(LAST_VERSION_KEY, currentVersion)
            try {
                Files.writeString(markerFile, currentVersion)
            } catch (_: Exception) {
            }
        }, ModalityState.nonModal())
    }

    private fun applyEditorScheme() {
        try {
            val ecm = EditorColorsManager.getInstance()
            val ddcScheme = ecm.getScheme(EDITOR_SCHEME_NAME) ?: return
            ecm.setGlobalScheme(ddcScheme)
        } catch (_: Exception) {
        }
    }

    private fun applyKeymap() {
        try {
            val keymapManager = KeymapManagerEx.getInstanceEx()
            val ddcKeymap = keymapManager.getKeymap(KEYMAP_NAME) ?: return
            keymapManager.activeKeymap = ddcKeymap
        } catch (_: Exception) {
        }
    }

    private fun installAndApplyCodeStyle() {
        try {
            val codeStylesDir = Path.of(PathManager.getConfigPath(), "codestyles")
            Files.createDirectories(codeStylesDir)
            val targetFile = codeStylesDir.resolve(CODE_STYLE_FILE)
            if (!Files.exists(targetFile)) {
                val resource = javaClass.getResourceAsStream("/extras/$CODE_STYLE_FILE") ?: return
                resource.use { Files.copy(it, targetFile, StandardCopyOption.REPLACE_EXISTING) }
            }
            CodeStyleSchemesImpl.getSchemeManager().reload()
            val schemes = CodeStyleSchemes.getInstance()
            val ddcScheme = schemes.allSchemes.firstOrNull { it.name == CODE_STYLE_SCHEME_NAME } ?: return
            schemes.currentScheme = ddcScheme
        } catch (_: Exception) {
        }
    }

    private fun installWindowLayout() {
        try {
            val layoutManager = ToolWindowDefaultLayoutManager.getInstance()
            if (WINDOW_LAYOUT_NAME in layoutManager.getLayoutNames()) return

            val ddcXml = readWindowLayoutResource() ?: return
            val ddcJsonStr = extractJsonFromXml(ddcXml) ?: return
            val json = Json { ignoreUnknownKeys = true }

            val ddcFullJson = json.parseToJsonElement(ddcJsonStr).jsonObject
            val ddcLayouts = ddcFullJson["layouts"]?.jsonObject ?: return
            val ddcLayoutEntry = ddcLayouts[WINDOW_LAYOUT_NAME] ?: return

            // Use reflection to avoid compile-time reference to @Internal state classes
            val getStateMethod =
                layoutManager.javaClass.methods
                    .firstOrNull { it.name == "getState" && it.returnType.name.contains("StorageManagerState") }
                    ?: return
            val currentState = getStateMethod.invoke(layoutManager) ?: return

            val companion = currentState.javaClass.getDeclaredField("Companion").get(null)
            val serializer =
                companion.javaClass.methods
                    .first { it.name == "serializer" }
                    .invoke(companion)

            val encodeMethod =
                json.javaClass.getMethod(
                    "encodeToString",
                    SerializationStrategy::class.java,
                    Any::class.java,
                )
            val currentJsonStr = encodeMethod.invoke(json, serializer, currentState) as String

            val currentJson = json.parseToJsonElement(currentJsonStr).jsonObject
            val existingLayouts = currentJson["layouts"]?.jsonObject?.toMutableMap() ?: mutableMapOf()
            existingLayouts[WINDOW_LAYOUT_NAME] = ddcLayoutEntry

            val mergedJson =
                buildJsonObject {
                    currentJson.forEach { (key, value) ->
                        if (key != "layouts") put(key, value)
                    }
                    put("layouts", JsonObject(existingLayouts))
                }
            val mergedJsonStr =
                json.encodeToString(
                    kotlinx.serialization.json.JsonElement
                        .serializer(),
                    mergedJson,
                )

            val decodeMethod =
                json.javaClass.getMethod(
                    "decodeFromString",
                    DeserializationStrategy::class.java,
                    String::class.java,
                )
            val newState = decodeMethod.invoke(json, serializer, mergedJsonStr)

            val loadStateMethod =
                layoutManager.javaClass.methods
                    .firstOrNull { it.name == "loadState" && it.parameterTypes[0].name.contains("StorageManagerState") }
                    ?: return
            loadStateMethod.invoke(layoutManager, newState)
        } catch (_: Exception) {
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

    private fun extractJsonFromXml(xml: String): String? {
        val start = xml.indexOf("<![CDATA[")
        val end = xml.indexOf("]]>")
        if (start < 0 || end < 0) return null
        return xml.substring(start + 9, end).trim()
    }
}
