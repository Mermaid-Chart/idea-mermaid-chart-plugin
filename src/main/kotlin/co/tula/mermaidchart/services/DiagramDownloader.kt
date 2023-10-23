package co.tula.mermaidchart.services

import co.tula.mermaidchart.settings.MermaidSettings
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.logging.Logger
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object DiagramDownloader {
    private val client = HttpClient(Java)
    private val logger = logger<DiagramDownloader>()
    suspend operator fun invoke(project: Project, uri: String): File {
        val tempFile = FileUtilRt.createTempFile("diagram_", ".png")
        val scope = CoroutineScope(coroutineContext)

        return suspendCancellableCoroutine { cont ->
            val task = object : Task.Backgroundable(project, "Diagram Downloading") {
                override fun run(indicator: ProgressIndicator) {
                    logger.debug("Task launched")
                    indicator.isIndeterminate = true
                    scope.launch {
                        client
                            .prepareGet(url = Url(uri)) {
                                header("Authorization", "Bearer ${MermaidSettings.token}")
                            }
                            .execute {
                                val fos = FileOutputStream(tempFile)
                                try {
                                    it.bodyAsChannel().copyTo(fos)
                                    logger.debug("Copied")
                                    cont.resume(tempFile)
                                } catch (e: Exception){
                                    logger.debug("Failed")
                                    cont.resumeWithException(e)
                                } finally {
                                    logger.debug("FOS Closed")
                                    fos.close()
                                }
                            }
                    }
                }
            }

            task.queue()
        }
    }
}
