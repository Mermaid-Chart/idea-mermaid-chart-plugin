package co.tula.mermaidchart.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel


class MermaidSettingsComponent() {

    private var panel: JPanel
    private val tokenInput = JBPasswordField()
    private val baseUrlInput = JBTextField()

    init {

        panel = FormBuilder.createFormBuilder()
            .also { builder ->
                if(MermaidSettings.baseUrl == MermaidSettings.BASE_URL_DEFAULT) {
                    builder.addComponent(buildHint())
                }
            }
            .addLabeledComponent(JBLabel("Token: "), tokenInput, 1, false)
            .addLabeledComponent(JBLabel("Base url: "), baseUrlInput, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun buildHint(): JPanel {
        val tokenUrl = "${MermaidSettings.BASE_URL_DEFAULT}/app/user/settings"
        val container = JPanel()
        container.layout = FlowLayout()

        container.add(JBLabel("You can get token here: "))
        container.add(JBLabel(tokenUrl).apply {
            foreground = EditorColorsManager.getInstance()
                .schemeForCurrentUITheme
                .getAttributes(EditorColors.REFERENCE_HYPERLINK_COLOR)
                .foregroundColor

            this.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e)
                    BrowserUtil.open(tokenUrl)
                }
            })
        })

        return container
    }

    fun getPanel(): JPanel {
        return panel
    }

    fun getPreferredFocusedComponent(): JComponent {
        return tokenInput
    }

    fun getToken(): String {
        return tokenInput.text
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