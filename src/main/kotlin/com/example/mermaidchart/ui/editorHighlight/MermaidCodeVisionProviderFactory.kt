@file:Suppress("UnstableApiUsage")

package com.example.mermaidchart.ui.editorHighlight

import com.example.mermaidchart.utils.isMermaidLink
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.TextCodeVisionEntry
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

// [MermaidChart: 5089868e-68e3-45cf-982a-21129803cb19]
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

            val length = editor.document.textLength

            val lenses = ArrayList<Pair<TextRange, CodeVisionEntry>>()

            val traverser = SyntaxTraverser.psiTraverser(file)
            for (element in traverser.preOrderDfsTraversal()) {
                if (!element.isMermaidLink()) continue

                val textRange = element.safeRange(length)
                val viewEntry = makeEntry("View Diagram", id)
                lenses.add(textRange to viewEntry)
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

            val length = editor.document.textLength

            val lenses = ArrayList<Pair<TextRange, CodeVisionEntry>>()

            val traverser = SyntaxTraverser.psiTraverser(file)
            for (element in traverser.preOrderDfsTraversal()) {
                if (!element.isMermaidLink()) continue

                val textRange = element.safeRange(length)
                val editEntry = makeEntry("Edit Diagram", id)
                lenses.add(textRange to editEntry)
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