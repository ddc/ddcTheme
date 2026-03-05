package com.ddc.theme.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class DdcThemeConfigurable : Configurable {

    private val settings = DdcThemeSettings.getInstance()
    private var enableHighlighting = settings.selectionHighlightEnabled

    override fun getDisplayName() = "DDC Theme"

    override fun createComponent(): JComponent = panel {
        group("Editor") {
            row {
                checkBox("Enable selection occurrence highlighting")
                    .applyToComponent { isSelected = enableHighlighting }
                    .onChanged { enableHighlighting = it.isSelected }
            }
        }
    }

    override fun isModified() = enableHighlighting != settings.selectionHighlightEnabled

    override fun apply() {
        settings.selectionHighlightEnabled = enableHighlighting
    }

    override fun reset() {
        enableHighlighting = settings.selectionHighlightEnabled
    }
}
