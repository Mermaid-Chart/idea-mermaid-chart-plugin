package co.tula.mermaidchart.services

import co.tula.mermaidchart.data.DiagramFormat
import co.tula.mermaidchart.data.DiagramTheme
import co.tula.mermaidchart.settings.MermaidSettings
import co.tula.mermaidchart.utils.extensions.withApi
import co.tula.mermaidchart.utils.rightOrThrow
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.logging.Logger
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object DiagramDownloader {
    private val client = HttpClient(Java)
    private val tempDir = File(FileUtilRt.getTempDirectory())

    suspend operator fun invoke(project: Project, diagramId: String, format: DiagramFormat): File {
        val theme = when(EditorColorsManager.getInstance().isDarkEditor){
            true -> DiagramTheme.Dark
            else -> DiagramTheme.Light
        }

        val (document, uri) = project.withApi {
            val document = it.document(diagramId).rightOrThrow()
            document to it.viewUrl(document, theme, format)
        }

        val tempFile =  File(tempDir, "MermaidChart_${diagramId}_v${document.major}_${document.minor}.${format.format}")

        if(tempFile.exists()){
            return tempFile
        }

        return suspendCancellableCoroutine { cont ->

            val task = object : Task.Backgroundable(project, "Diagram Downloading") {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    runBlocking {
                        client
                            .prepareGet(url = Url(uri)) {
                                header("Authorization", "Bearer ${MermaidSettings.token}")
                            }
                            .execute {
                                val fos = FileOutputStream(tempFile)
                                try {
                                    it.bodyAsChannel().copyTo(fos)
                                    cont.resume(tempFile)
                                } catch (e: Exception){
                                    cont.resumeWithException(e)
                                } finally {
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
