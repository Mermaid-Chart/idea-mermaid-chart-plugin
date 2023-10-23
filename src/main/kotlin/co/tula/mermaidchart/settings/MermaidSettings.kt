package co.tula.mermaidchart.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.util.PropertiesComponent

object MermaidSettings {
    private const val KEY_TOKEN = "co.tula.mermaidchart.settings.token"
    private const val KEY_BASE_URL = "co.tula.mermaidchart.settings.baseUrl"
    private const val BASE_URL_DEFAULT = "https://www.mermaidchart.com"

    private val props = PropertiesComponent.getInstance()

    private val tokenCredentials = CredentialAttributes(generateServiceName("MermaidCharts", KEY_TOKEN))
    var token: String
        set(value) =
            PasswordSafe.instance.set(tokenCredentials, Credentials("default", value))
        get() =
            PasswordSafe.instance.getPassword(tokenCredentials) ?: ""


    var baseUrl: String
        get() = props.getValue(KEY_BASE_URL, BASE_URL_DEFAULT).takeIf { it.isNotEmpty() } ?: BASE_URL_DEFAULT
        set(value) = props.setValue(KEY_BASE_URL, value.replaceFirst("/*$", ""))
}