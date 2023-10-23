@file:Suppress("UnstableApiUsage")

package co.tula.mermaidchart.ui.editorHighlight

import co.tula.mermaidchart.services.DiagramDownloader
import co.tula.mermaidchart.utils.extensions.withApiSync
import co.tula.mermaidchart.utils.mermaidLinks
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.codeInsight.codeVision.ui.model.TextCodeVisionEntry
import com.intellij.ide.BrowserUtil
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

                val viewEntry = makeEntry("View Diagram", id) { _, _ ->
                    scope.launch {
                        val file = try {
                            DiagramDownloader(
                                project,
                                "https://www.mermaidchart.com/raw/cd149d67-d546-479b-bd6d-3b59e9c093ca?version=v0.1&theme=light&format=png"
                            )
                        } catch (e: Exception) {
                            //TODO: Show error
                            null
                        }
                        val virtualFile = file?.let { LocalFileSystem.getInstance().findFileByIoFile(it) }
                        runInEdt {
                            println("Ran in EDT")
                            FileEditorManager.getInstance(project).openFile(virtualFile!!)
                        }
                    }
                }
                links.forEach { link ->
                    lenses.add(link.range to viewEntry)
                }
            }
            return@runReadAction CodeVisionState.Ready(lenses)
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
                        project.withApiSync {
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
