package co.tula.mermaidchart.data

import co.tula.mermaidchart.data.models.Document
import co.tula.mermaidchart.data.models.Project
import co.tula.mermaidchart.data.models.User
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class MermaidApi(
    private val baseUrl: String,
    private val token: String
) {
    fun editUrl(documentId: String): String {
        return "$baseUrl/app/diagrams/${documentId}?ref=idea"
    }

    suspend fun me(): User {
        return "$baseUrl/rest-api/users/me".httpGet()
    }

    suspend fun projects(): List<Project> {
        return "$baseUrl/rest-api/projects".httpGet()
    }

    suspend fun documents(projectId: String): List<Document> {
        return "$baseUrl/rest-api/projects/$projectId/documents".httpGet()
    }

    suspend inline fun <reified T> String.httpGet(vararg param: Pair<String, String>): T {
        return json.decodeFromString(httpRaw(*param))
    }

    suspend fun String.httpRaw(vararg param: Pair<String, String>): String {
        val request = requestBuilder(this).apply {
            method = HttpMethod.Get
            param.forEach { (key, value) ->
                parameter(key, value)
            }
        }
        val httpResponse = ktorClient.request(request)
        return httpResponse.bodyAsText()
    }

    private fun requestBuilder(uriString: String): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url {
                takeFrom(uriString)
            }
            header("Authorization", "Bearer $token")
        }
    }

    companion object {
        private val ktorClient = HttpClient(Java)
        val json = Json {
            this.ignoreUnknownKeys = true
            this.coerceInputValues = true
        }
    }
}