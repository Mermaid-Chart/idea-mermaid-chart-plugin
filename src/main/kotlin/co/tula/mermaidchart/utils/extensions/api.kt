package co.tula.mermaidchart.utils.extensions

import co.tula.mermaidchart.data.MermaidApi
import co.tula.mermaidchart.settings.MermaidSettings
import com.intellij.openapi.project.Project

inline fun <T> Project.withApi(body: (MermaidApi) -> T): T {
    val gitRepository = MermaidApi(MermaidSettings.baseUrl, MermaidSettings.token)
    return body(gitRepository)
}