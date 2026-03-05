package com.ddc.theme.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "DdcThemeSettings", storages = [Storage("ddc-theme.xml")])
class DdcThemeSettings : PersistentStateComponent<DdcThemeSettings.State> {

    data class State(
        var selectionHighlightEnabled: Boolean = false,
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var selectionHighlightEnabled: Boolean
        get() = state.selectionHighlightEnabled
        set(value) { state.selectionHighlightEnabled = value }

    companion object {
        fun getInstance(): DdcThemeSettings =
            ApplicationManager.getApplication().getService(DdcThemeSettings::class.java)
    }
}
