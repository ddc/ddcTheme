package com.ddc.theme.listeners

import com.ddc.theme.settings.DdcThemeSettings
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
import com.intellij.openapi.wm.WindowManager
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
                    clearHighlights(editor, project)
                    if (!DdcThemeSettings.getInstance().selectionHighlightEnabled) return
                    val selectedText = editor.selectionModel.selectedText
                    if (selectedText.isNullOrBlank()) return
                    highlightOccurrences(editor, selectedText, project)
                }
            },
            project,
        )
    }

    private fun clearHighlights(
        editor: Editor,
        project: Project,
    ) {
        val highlighters = editor.getUserData(HIGHLIGHTERS_KEY) ?: return
        val markupModel = editor.markupModel
        for (highlighter in highlighters) {
            markupModel.removeHighlighter(highlighter)
        }
        highlighters.clear()
        WindowManager.getInstance().getStatusBar(project)?.setInfo("")
    }

    private fun isWordChar(ch: Char): Boolean = ch.isLetterOrDigit() || ch == '_'

    private fun isWholeWord(
        text: String,
        start: Int,
        end: Int,
    ): Boolean {
        if (start > 0 && isWordChar(text[start - 1])) return false
        if (end < text.length && isWordChar(text[end])) return false
        return true
    }

    private fun highlightOccurrences(
        editor: Editor,
        selectedText: String,
        project: Project,
    ) {
        val document = editor.document
        val text = document.text
        val textLower = text.lowercase()
        val searchLower = selectedText.lowercase()
        val markupModel = editor.markupModel
        val highlighters = mutableListOf<RangeHighlighter>()
        val selStart = editor.selectionModel.selectionStart
        val selEnd = editor.selectionModel.selectionEnd
        val settings = DdcThemeSettings.getInstance()
        val maxOccurrences = settings.maxHighlightOccurrences
        val wholeWordOnly = settings.wholeWordHighlightOnly
        val attributes =
            TextAttributes().apply {
                backgroundColor = HIGHLIGHT_COLOR
            }
        var limitReached = false

        var index = textLower.indexOf(searchLower)
        while (index >= 0) {
            val end = index + selectedText.length
            if (index != selStart || end != selEnd) {
                if (!wholeWordOnly || isWholeWord(text, index, end)) {
                    val highlighter =
                        markupModel.addRangeHighlighter(
                            index,
                            end,
                            HighlighterLayer.SELECTION - 1,
                            attributes,
                            HighlighterTargetArea.EXACT_RANGE,
                        )
                    highlighters.add(highlighter)
                    if (highlighters.size >= maxOccurrences) {
                        limitReached = true
                        break
                    }
                }
            }
            index = textLower.indexOf(searchLower, index + 1)
        }

        editor.putUserData(HIGHLIGHTERS_KEY, highlighters)

        if (highlighters.isNotEmpty()) {
            val count = highlighters.size
            val msg = if (limitReached) "$count+ occurrences highlighted (limit reached)" else "$count occurrences highlighted"
            WindowManager.getInstance().getStatusBar(project)?.setInfo(msg)
        }
    }
}
