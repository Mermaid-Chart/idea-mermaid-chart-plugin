package co.tula.mermaidchart.utils.extensions

import co.tula.mermaidchart.data.MermaidApi
import co.tula.mermaidchart.settings.MermaidSettings
import com.intellij.openapi.project.Project

suspend fun <T> Project.withApi(fn: suspend (MermaidApi) -> T): T {
    val gitRepository = MermaidApi(MermaidSettings.baseUrl, MermaidSettings.token)
    return fn(gitRepository)
}

fun <T> Project.withApiSync(fn: (MermaidApi) -> T): T {
    val gitRepository = MermaidApi(MermaidSettings.baseUrl, MermaidSettings.token)
    return fn(gitRepository)
}