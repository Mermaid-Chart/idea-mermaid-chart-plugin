package co.tula.mermaidchart.data

import co.tula.mermaidchart.data.models.Document
import co.tula.mermaidchart.data.models.Project
import co.tula.mermaidchart.data.models.ProjectWithDocuments
import co.tula.mermaidchart.data.models.User
import co.tula.mermaidchart.utils.EitherE
import co.tula.mermaidchart.utils.Left
import co.tula.mermaidchart.utils.Right
import co.tula.mermaidchart.utils.bind
import com.fasterxml.jackson.annotation.JsonValue
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

    suspend fun me(): EitherE<User> = wrapResult {
        "$baseUrl/rest-api/users/me".httpGet()
    }

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

    suspend fun projects(): EitherE<List<Project>> = wrapResult {
        "$baseUrl/rest-api/projects".httpGet()
    }

    suspend fun documents(projectId: String): EitherE<List<Document>> = wrapResult {
        "$baseUrl/rest-api/projects/$projectId/documents".httpGet()
    }

    suspend fun document(documentId: String): EitherE<Document> = wrapResult {
        "$baseUrl/rest-api/documents/$documentId".httpGet()
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
        if(!httpResponse.status.isSuccess()){
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

    private suspend fun <T> wrapResult(fn: suspend () -> T): EitherE<T> {
        return try {
            Right(fn())
        } catch (e: Exception) {
            Left(e)
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