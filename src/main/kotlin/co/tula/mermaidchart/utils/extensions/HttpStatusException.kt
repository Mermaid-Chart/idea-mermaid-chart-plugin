package co.tula.mermaidchart.utils.extensions

import com.intellij.util.io.HttpRequests
import io.ktor.http.*

val HttpRequests.HttpStatusException.isUnauthorized
    get() = this.statusCode == HttpStatusCode.Unauthorized.value