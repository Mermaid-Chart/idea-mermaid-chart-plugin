package co.tula.mermaidchart.settings

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent


class MermaidSettingsConfigurable : Configurable {
    private var view: MermaidSettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "MermaidCharts"
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
        return view?.let {
            it.getToken() != MermaidSettings.token
                    || it.getBaseUrl() != MermaidSettings.baseUrl
        } ?: false
    }

    override fun apply() {
        view?.let {
            MermaidSettings.token = it.getToken()
            MermaidSettings.baseUrl = it.getBaseUrl()
        }
        ApplicationManager.getApplication().messageBus.syncPublisher(MermaidSettingsTopic.TOPIC).onSettingsChange()
    }

    override fun reset() {
        view?.let {
            it.setToken(MermaidSettings.token)
            it.setBaseUrl(MermaidSettings.baseUrl)
        }
    }

    override fun disposeUIResources() {
        view = null
    }
}