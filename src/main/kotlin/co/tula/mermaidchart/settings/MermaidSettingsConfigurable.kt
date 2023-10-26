package co.tula.mermaidchart.settings

import co.tula.mermaidchart.utils.MessageProvider.message
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent


class MermaidSettingsConfigurable : Configurable {
    private var view: MermaidSettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return message("settings.name")
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return view?.getPreferredFocusedComponent()
    }

    override fun createComponent(): JComponent {
        val component = MermaidSettingsComponent()
        view = component
        return component.getPanel()
    }

    override fun isModified(): Boolean {
        return view?.getPanel()?.isModified() ?: false
    }

    override fun apply() {
        view?.let {
            it.getPanel().apply()

            MermaidSettings.token = it.token
            MermaidSettings.baseUrl = it.baseUrl
        }
        ApplicationManager.getApplication().messageBus.syncPublisher(MermaidSettingsTopic.TOPIC).onSettingsChange()
    }

    override fun reset() {
        return view?.getPanel()?.reset() ?: Unit
    }

    override fun disposeUIResources() {
        view = null
    }
}