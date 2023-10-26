package co.tula.mermaidchart.settings

import co.tula.mermaidchart.utils.MessageProvider.message
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.enteredTextSatisfies
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.text.JTextComponent


class MermaidSettingsComponent() {

    private var panel: DialogPanel
    var token = MermaidSettings.token
        set(value) {
            hintVisible.set(value.isEmpty())
            field = value
        }
    var baseUrl = MermaidSettings.baseUrl

    private var hintVisible = AtomicBooleanProperty(token.isNotEmpty())

    private lateinit var tokenInput: Cell<JTextComponent>

    init {
        panel = buildPanel()
    }

    private fun buildPanel(): DialogPanel {
        return panel {

            row(message("settings.labels.token") + ": ") {
                tokenInput = passwordField().bindText(::token).widthGroup("input")
            }

            row(message("settings.labels.baseUrl") + ": ") {
                textField().bindText(::baseUrl).widthGroup("input")
            }

            row {
                label(message("settings.labels.tokenHintLink") + ": ")
                val link = "${MermaidSettings.BASE_URL_DEFAULT}/app/user/settings"
                browserLink(link, link)
            }.visibleIf(tokenInput.component.enteredTextSatisfies { it.isEmpty() })
        }
    }

    fun getPanel(): DialogPanel {
        return panel
    }

    fun getPreferredFocusedComponent(): JComponent {
        return tokenInput.component
    }
}