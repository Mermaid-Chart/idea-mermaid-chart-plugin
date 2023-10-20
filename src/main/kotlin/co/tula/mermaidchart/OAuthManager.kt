package co.tula.mermaidchart

import com.intellij.ide.BrowserUtil
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URIBuilder
import org.jetbrains.ide.BuiltInServerManager
import java.security.MessageDigest
import java.util.*
/*
    OAuth2 flow
    Open browser with
    https://www.mermaidchart.com/oauth/authorize?
        response_type="code"
        &client_id=...
        &redirect_uri=127.0.0.1:idea_port/api/mermaid/oauth/callback
        &code_challenge_method=S256
        &code_challenge=sha256(uuid)
        &state=uuid
        &scope=email

    https://www.mermaidchart.com/oauth/authorize?
        client_id=469e30a6-2602-4022-aff8-2ab36842dc57
        &redirect_uri=vscode://MermaidChart.vscode-mermaid-chart
        &response_type=code
        &code_challenge_method=S256
        &code_challenge=hE1ZQar76qshJLXNuRF7o8eUF5ir-hf3y9jl8mkJSTM
        &state=bdf3fd1f-aed5-4a2d-992d-a2c9664121a6
        &scope=email

    for state and code_challenge we have to make pair of uuid, one is state (key) to code_challenge,
       we have to store this pair for auth process
 */
class OAuthManager {
    private val pendingStates = mutableMapOf<UUID, UUID>()


    fun initAuthFlow() {
        val state = UUID.randomUUID()
        val codeChallenge = UUID.randomUUID()
        pendingStates[state] = codeChallenge
        val challengeSHA256 = encodeHash(hashString(codeChallenge.toString(), "SHA-256"))

        val uri = URIBuilder().apply {
            scheme = "https"
            host = BASE_URL
            path = "app/login"
            setParameters(
                mapOf(
                    "client_id" to CLIENT_ID,
                    "redirect_uri" to redirectUri(BuiltInServerManager.getInstance().port),
                    "response_type" to "code",
                    "code_challenge_method" to "S256",
                    "code_challenge" to challengeSHA256,
                    "state" to state.toString(),
                    "scope" to "email",
                ).map {
                    object : NameValuePair {
                        override fun getName(): String = it.key
                        override fun getValue(): String = it.value
                    }
                }
            )
        }
            .build()

        BrowserUtil.open(uri.toString())
    }

    private fun encodeHash(input: String): String {
        return String(Base64.getEncoder().encode(input.toByteArray()))
            .replace("+", "-")
            .replace("/", "_")
            .replace(Regex("=+$"), "")
    }

    private fun hashString(input: String, algorithm: String): String {
        return MessageDigest
            .getInstance(algorithm)
            .digest(input.toByteArray())
            .fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }
            .toString()
    }

    companion object {
        const val BASE_URL = "www.mermaidchart.com"
        const val CLIENT_ID = "469e30a6-2602-4022-aff8-2ab36842dc57"
        fun redirectUri(port: Int) = "http://127.0.0.1:$port/api/mermaid/oauth/callback"
    }
}