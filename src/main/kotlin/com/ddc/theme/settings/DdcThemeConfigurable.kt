package com.ddc.theme.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel

class DdcThemeConfigurable : Configurable {
    private val settings = DdcThemeSettings.getInstance()
    private var enableHighlighting = settings.selectionHighlightEnabled
    private var wholeWordHighlightOnly = settings.wholeWordHighlightOnly
    private var maxHighlightOccurrences = settings.maxHighlightOccurrences
    private lateinit var warningLabel: JLabel

    private companion object {
        private const val MAX_SAFE_OCCURRENCES = 10_000
        private const val DEFAULT_OCCURRENCES = 1000
    }

    override fun getDisplayName() = "DDC Theme"

    override fun createComponent(): JComponent =
        panel {
            group("Editor") {
                lateinit var highlightEnabled: ComponentPredicate
                row {
                    checkBox("Enable selection occurrence highlighting")
                        .applyToComponent { isSelected = enableHighlighting }
                        .onChanged { enableHighlighting = it.isSelected }
                        .also { highlightEnabled = it.component.asComponentPredicate() }
                }
                row {
                    checkBox("Match whole word only")
                        .applyToComponent { isSelected = wholeWordHighlightOnly }
                        .onChanged { wholeWordHighlightOnly = it.isSelected }
                }.enabledIf(highlightEnabled)
                row("Maximum occurrences to highlight:") {
                    intTextField(1..100_000)
                        .applyToComponent { text = maxHighlightOccurrences.toString() }
                        .onChanged {
                            maxHighlightOccurrences = it.text.toIntOrNull() ?: DEFAULT_OCCURRENCES
                            updateWarning()
                        }
                }.enabledIf(highlightEnabled)
                row {
                    warningLabel =
                        JLabel().apply {
                            foreground = java.awt.Color(0xFF, 0x99, 0x00)
                        }
                    cell(warningLabel)
                }
            }
            updateWarning()
        }

    private fun JCheckBox.asComponentPredicate(): ComponentPredicate {
        val checkbox = this
        return object : ComponentPredicate() {
            override fun invoke(): Boolean = checkbox.isSelected

            override fun addListener(listener: (Boolean) -> Unit) {
                checkbox.addChangeListener { listener(checkbox.isSelected) }
            }
        }
    }

    private fun updateWarning() {
        if (::warningLabel.isInitialized) {
            warningLabel.text =
                if (maxHighlightOccurrences > MAX_SAFE_OCCURRENCES) {
                    "High values may affect editor performance (default: $DEFAULT_OCCURRENCES)"
                } else {
                    ""
                }
        }
    }

    override fun isModified() =
        enableHighlighting != settings.selectionHighlightEnabled ||
            wholeWordHighlightOnly != settings.wholeWordHighlightOnly ||
            maxHighlightOccurrences != settings.maxHighlightOccurrences

    override fun apply() {
        settings.selectionHighlightEnabled = enableHighlighting
        settings.wholeWordHighlightOnly = wholeWordHighlightOnly
        if (maxHighlightOccurrences > MAX_SAFE_OCCURRENCES) {
            maxHighlightOccurrences = DEFAULT_OCCURRENCES
            updateWarning()
        }
        settings.maxHighlightOccurrences = maxHighlightOccurrences
    }

    override fun reset() {
        enableHighlighting = settings.selectionHighlightEnabled
        wholeWordHighlightOnly = settings.wholeWordHighlightOnly
        maxHighlightOccurrences = settings.maxHighlightOccurrences
        updateWarning()
    }
}
