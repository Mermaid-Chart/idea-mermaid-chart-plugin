package co.tula.mermaidchart.data

import co.tula.mermaidchart.data.models.Document
import co.tula.mermaidchart.data.models.Project
import co.tula.mermaidchart.data.models.ProjectWithDocuments
import co.tula.mermaidchart.data.models.User
import co.tula.mermaidchart.utils.EitherE
import co.tula.mermaidchart.utils.Left
import co.tula.mermaidchart.utils.Right
import com.fasterxml.jackson.annotation.JsonValue
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.io.HttpRequests.HttpStatusException
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

enum class DiagramTheme(
    @JsonValue val theme: String
) {
    Light("light"),
    Dark("dark")
}

enum class DiagramFormat(
    @JsonValue val format: String
) {
    PNG("png"),
    SVG("svg"),
    HTML("html")
}

class MermaidApi(
    private val baseUrl: String,
    private val token: String
) {
    fun viewUrl(document: Document, theme: DiagramTheme, format: DiagramFormat = DiagramFormat.PNG): String {
        return "$baseUrl/raw/${document.documentId}?version=v${document.major}.${document.minor}&theme=${theme.theme}&format=${format.format}"
    }

    fun editUrl(documentId: String): String {
        return "$baseUrl/app/diagrams/${documentId}?ref=idea"
    }

    suspend fun me(): EitherE<User> = wrapGetResult("$baseUrl/rest-api/users/me")

    suspend fun projectsWithDocuments(): EitherE<List<ProjectWithDocuments>> {
        val projects = projects()
        if (projects is Left) return Left(projects.v)

        val documents = (projects as Right).v
            .map {
                val docs = documents(it.id)
                if (docs is Left) return Left(docs.v)
                (docs as Right).v
            }

        return projects.v
            .zip(documents)
            .map { ProjectWithDocuments(it.first, it.second) }
            .let { Right(it) }
    }

    suspend fun projects(): EitherE<List<Project>> = wrapGetResult("$baseUrl/rest-api/projects?ref=idea")

    suspend fun documents(projectId: String): EitherE<List<Document>> =
        wrapGetResult("$baseUrl/rest-api/projects/$projectId/documents")

    suspend fun document(documentId: String): EitherE<Document> =
        wrapGetResult("$baseUrl/rest-api/documents/$documentId")

    suspend inline fun <reified T> String.httpGet(vararg param: Pair<String, String>): T {
        val raw = httpRaw(*param)
        return json.decodeFromString(raw)
    }

    suspend fun String.httpRaw(vararg param: Pair<String, String>): String {
        val request = requestBuilder(this).apply {
            method = HttpMethod.Get
            param.forEach { (key, value) ->
                parameter(key, value)
            }
        }
        val httpResponse = ktorClient.request(request)
        if (!httpResponse.status.isSuccess()) {
            throw HttpStatusException("Wrong response code", httpResponse.status.value, request.url.buildString())
        }
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

    private suspend inline fun <reified T> wrapGetResult(url: String): EitherE<T> {
        return try {
            Right(url.httpGet<T>())
        } catch (e: Exception) {
            Left(MermaidApiException(url, e))
        }
    }

    class MermaidApiException(url: String, cause: Exception) : Exception("Failed to request: $url", cause)

    companion object {
        private val ktorClient = HttpClient(Java)
        val json = Json {
            this.ignoreUnknownKeys = true
            this.coerceInputValues = true
        }
    }
}