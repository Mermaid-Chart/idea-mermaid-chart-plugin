@file:Suppress("UnstableApiUsage")

package co.tula.mermaidchart.ui.editorHighlight

import co.tula.mermaidchart.data.DiagramFormat
import co.tula.mermaidchart.services.DiagramDownloader
import co.tula.mermaidchart.utils.Left
import co.tula.mermaidchart.utils.MermaidLink
import co.tula.mermaidchart.utils.Right
import co.tula.mermaidchart.utils.extensions.withApi
import co.tula.mermaidchart.utils.mermaidLinks
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.codeInsight.codeVision.ui.model.TextCodeVisionEntry
import com.intellij.codeInsight.hint.HintManager
import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.SyntaxTraverser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.event.MouseEvent

class MermaidCodeVisionProviderFactory : CodeVisionProviderFactory {
    override fun createProviders(project: Project): Sequence<CodeVisionProvider<*>> {
        return sequenceOf(MermaidActionCodeVisionViewProvider(project), MermaidActionCodeVisionEditProvider(project))
    }
}

//TODO: Fix display in Inlay Hints settings

class MermaidActionCodeVisionViewProvider(
    private val project: Project
) : CodeVisionProvider<Unit> {
    val scope = CoroutineScope(Dispatchers.Default)
    override val defaultAnchor: CodeVisionAnchorKind
        get() = CodeVisionAnchorKind.Top
    override val id: String
        get() = MermaidActionCodeVisionViewProvider::class.java.canonicalName
    override val name: String
        get() = "Mermaid View Action Code Vision"
    override val relativeOrderings: List<CodeVisionRelativeOrdering>
        get() = listOf(CodeVisionRelativeOrdering.CodeVisionRelativeOrderingFirst)

    override fun precomputeOnUiThread(editor: Editor) {}

    override fun computeCodeVision(editor: Editor, uiData: Unit): CodeVisionState {
        return runReadAction {
            val project = editor.project ?: return@runReadAction CodeVisionState.READY_EMPTY
            val document = editor.document
            val file = PsiDocumentManager.getInstance(project).getPsiFile(document)
                ?: return@runReadAction CodeVisionState.NotReady

            val lenses = ArrayList<Pair<TextRange, CodeVisionEntry>>()

            val traverser = SyntaxTraverser.psiTraverser(file)
            for (element in traverser.preOrderDfsTraversal()) {
                val links = element.mermaidLinks()

                links.forEach { link ->
                    val viewEntry = buildEntryForLink(link, editor)

                    lenses.add(link.range to viewEntry)
                }
            }
            return@runReadAction CodeVisionState.Ready(lenses)
        }
    }

    private fun buildEntryForLink(link: MermaidLink, editor: Editor): TextCodeVisionEntry {
        fun showError(msg: String) {
            runInEdt {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("MermaidCharts")
                    .createNotification("Mermaid Charts", msg, NotificationType.ERROR)
                    .notify(editor.project)
            }
        }
        return makeEntry("View Diagram", id) { _, _ ->
            scope.launch {
                when (val diagram = DiagramDownloader(project, link.documentId, DiagramFormat.PNG)) {
                    is Left -> showError("Can't download file")

                    is Right -> {
                        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(diagram.v)
                        if (virtualFile == null) {
                            showError("Can't find downloaded file")
                            return@launch
                        }

                        runInEdt {
                            FileEditorManager.getInstance(project).openFile(virtualFile)
                        }
                    }
                }
            }
        }
    }
}

class MermaidActionCodeVisionEditProvider(
    private val project: Project
) : CodeVisionProvider<Unit> {
    override val defaultAnchor: CodeVisionAnchorKind
        get() = CodeVisionAnchorKind.Top
    override val id: String
        get() = MermaidActionCodeVisionEditProvider::class.java.canonicalName
    override val name: String
        get() = "Mermaid Edit Action Code Vision"
    override val relativeOrderings: List<CodeVisionRelativeOrdering>
        get() = listOf(CodeVisionRelativeOrdering.CodeVisionRelativeOrderingLast)

    override fun precomputeOnUiThread(editor: Editor) {}

    override fun computeCodeVision(editor: Editor, uiData: Unit): CodeVisionState {
        return runReadAction {
            val project = editor.project ?: return@runReadAction CodeVisionState.READY_EMPTY
            val document = editor.document
            val file = PsiDocumentManager.getInstance(project).getPsiFile(document)
                ?: return@runReadAction CodeVisionState.NotReady

            val lenses = ArrayList<Pair<TextRange, CodeVisionEntry>>()

            val traverser = SyntaxTraverser.psiTraverser(file)
            for (element in traverser.preOrderDfsTraversal()) {
                val links = element.mermaidLinks()


                links.forEach { link ->
                    val editEntry = makeEntry("Edit Diagram", id) { e, editor ->
                        project.withApi {
                            BrowserUtil.open(it.editUrl(link.documentId))
                        }
                    }
                    lenses.add(link.range to editEntry)
                }
            }
            return@runReadAction CodeVisionState.Ready(lenses)
        }
    }
}

private fun makeEntry(
    name: String,
    id: String,
    onClick: (ev: MouseEvent?, editor: Editor) -> Unit
): TextCodeVisionEntry = ClickableTextCodeVisionEntry(
    name, id, onClick, null, name, name, emptyList()
).apply { showInMorePopup = false }
