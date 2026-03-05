package com.ddc.theme.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Key
import com.ddc.theme.settings.DdcThemeSettings
import java.awt.Color

class SelectionHighlightListener : ProjectActivity {

    companion object {
        private val HIGHLIGHTERS_KEY = Key.create<MutableList<RangeHighlighter>>("DDC_SELECTION_HIGHLIGHTERS")
        private val HIGHLIGHT_COLOR = Color(0x31, 0x43, 0x65)
    }

    override suspend fun execute(project: Project) {
        EditorFactory.getInstance().eventMulticaster.addSelectionListener(
            object : SelectionListener {
                override fun selectionChanged(e: SelectionEvent) {
                    val editor = e.editor
                    clearHighlights(editor)
                    if (!DdcThemeSettings.getInstance().selectionHighlightEnabled) return
                    val selectedText = editor.selectionModel.selectedText
                    if (selectedText.isNullOrBlank()) return
                    highlightOccurrences(editor, selectedText)
                }
            },
            project,
        )
    }

    private fun clearHighlights(editor: Editor) {
        val highlighters = editor.getUserData(HIGHLIGHTERS_KEY) ?: return
        val markupModel = editor.markupModel
        for (highlighter in highlighters) {
            markupModel.removeHighlighter(highlighter)
        }
        highlighters.clear()
    }

    private fun highlightOccurrences(editor: Editor, selectedText: String) {
        val document = editor.document
        val text = document.text
        val textLower = text.lowercase()
        val searchLower = selectedText.lowercase()
        val markupModel = editor.markupModel
        val highlighters = mutableListOf<RangeHighlighter>()
        val selStart = editor.selectionModel.selectionStart
        val selEnd = editor.selectionModel.selectionEnd
        val attributes = TextAttributes().apply {
            backgroundColor = HIGHLIGHT_COLOR
        }

        var index = textLower.indexOf(searchLower)
        while (index >= 0) {
            val end = index + selectedText.length
            if (index != selStart || end != selEnd) {
                val highlighter = markupModel.addRangeHighlighter(
                    index,
                    end,
                    HighlighterLayer.SELECTION - 1,
                    attributes,
                    HighlighterTargetArea.EXACT_RANGE,
                )
                highlighters.add(highlighter)
            }
            index = textLower.indexOf(searchLower, index + 1)
        }

        editor.putUserData(HIGHLIGHTERS_KEY, highlighters)
    }
}
