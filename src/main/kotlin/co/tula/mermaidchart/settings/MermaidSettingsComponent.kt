package co.tula.mermaidchart.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel


class MermaidSettingsComponent() {

    private var panel: JPanel
    private val tokenInput = JBPasswordField()
    private val baseUrlInput = JBTextField()

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Token: "), tokenInput, 1, false)
            .addLabeledComponent(JBLabel("Base url: "), baseUrlInput, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun getPanel(): JPanel {
        return panel
    }

    fun getPreferredFocusedComponent(): JComponent {
        return tokenInput
    }

    fun getToken(): String {
        return tokenInput.getText()
    }

    fun setToken(newText: String) {
        tokenInput.setText(newText)
    }

    fun getBaseUrl(): String {
        return baseUrlInput.getText()
    }

    fun setBaseUrl(newText: String) {
        baseUrlInput.setText(newText)
    }

}