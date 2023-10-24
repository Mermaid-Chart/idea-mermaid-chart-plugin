package co.tula.mermaidchart.services

import co.tula.mermaidchart.data.DiagramFormat
import co.tula.mermaidchart.data.DiagramTheme
import co.tula.mermaidchart.settings.MermaidSettings
import co.tula.mermaidchart.utils.EitherE
import co.tula.mermaidchart.utils.Left
import co.tula.mermaidchart.utils.Right
import co.tula.mermaidchart.utils.extensions.withApi
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object DiagramDownloader {
    private val client = HttpClient(Java)
    private val tempDir = File(FileUtilRt.getTempDirectory())

    suspend operator fun invoke(project: Project, diagramId: String, format: DiagramFormat): EitherE<File> {
        val theme = when (EditorColorsManager.getInstance().isDarkEditor) {
            true -> DiagramTheme.Dark
            else -> DiagramTheme.Light
        }

        val (document, uri) = project.withApi {
            val document = it.document(diagramId)
            if (document is Left) return Left(document.v)
            ((document as Right).v) to it.viewUrl(document.v, theme, format)
        }

        val tempFile = File(tempDir, "MermaidChart_${diagramId}_v${document.major}_${document.minor}.${format.format}")

        if (tempFile.exists()) {
            return Right(tempFile)
        }


        return suspendCancellableCoroutine { cont ->
            val fos = FileOutputStream(tempFile)

            val exceptionHandler = CoroutineExceptionHandler { _, t ->
                cont.resumeWithException(t)
                fos.close()
            }

            val task = object : Task.Backgroundable(project, "Diagram Downloading") {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    runBlocking(exceptionHandler) {
                        client
                            .prepareGet(url = Url(uri)) {
                                header("Authorization", "Bearer ${MermaidSettings.token}")
                            }
                            .execute {
                                it.bodyAsChannel().copyTo(fos)
                                cont.resume(Right(tempFile))
                            }
                    }

                    fos.close()
                }
            }

            task.queue()
        }
    }
}
