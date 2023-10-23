@file:Suppress("UnstableApiUsage")

package co.tula.mermaidchart.ui.editorHighlight

import co.tula.mermaidchart.utils.extensions.withApiSync
import co.tula.mermaidchart.utils.mermaidLinks
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.codeInsight.codeVision.ui.model.TextCodeVisionEntry
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.SyntaxTraverser
import java.awt.event.MouseEvent

class MermaidCodeVisionProviderFactory : CodeVisionProviderFactory {
    override fun createProviders(project: Project): Sequence<CodeVisionProvider<*>> {
        return sequenceOf(MermaidActionCodeVisionViewProvider(project), MermaidActionCodeVisionEditProvider(project))
    }
}

class MermaidActionCodeVisionViewProvider(
    private val project: Project
) : CodeVisionProvider<Unit> {
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
                    println("Hi there")
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
