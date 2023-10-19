@file:Suppress("UnstableApiUsage")

package co.tula.mermaidchart.ui.editorHighlight

import co.tula.mermaidchart.utils.mermaidLinkRange
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.TextCodeVisionEntry
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

class MermaidCodeVisionProviderFactory : CodeVisionProviderFactory {
    override fun createProviders(project: Project): Sequence<CodeVisionProvider<*>> {
        return sequenceOf(MermaidActionCodeVisionViewProvider(), MermaidActionCodeVisionEditProvider())
    }
}

class MermaidActionCodeVisionViewProvider : CodeVisionProvider<Unit> {
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
                val linkRanges = element.mermaidLinkRange()

                val viewEntry = makeEntry("View Diagram", id)
                linkRanges.forEach { linkRange ->
                    lenses.add(linkRange to viewEntry)
                }
            }
            return@runReadAction CodeVisionState.Ready(lenses)
        }
    }
}

class MermaidActionCodeVisionEditProvider : CodeVisionProvider<Unit> {
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
                val linkRanges = element.mermaidLinkRange()

                val editEntry = makeEntry("Edit Diagram", id)
                linkRanges.forEach { linkRange ->
                    lenses.add(linkRange to editEntry)
                }
            }
            return@runReadAction CodeVisionState.Ready(lenses)
        }
    }
}

private fun makeEntry(name: String, id: String): TextCodeVisionEntry = TextCodeVisionEntry(
    name, id, null, name, name, emptyList()
).apply { showInMorePopup = false }

private fun PsiComment.safeRange(editorLength: Int): TextRange {
    val textRange = InlayHintsUtils.getTextRangeWithoutLeadingCommentsAndWhitespaces(this)
    val adjustedRange = TextRange(
        Integer.min(textRange.startOffset, editorLength), Integer.min(textRange.endOffset, editorLength)
    )
    return adjustedRange
}