package co.tula.mermaidchart.utils

import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

private val mermaidLinkRegex =
    Regex("\\[MermaidChart: ([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})]")
private val mermaidDocumentIdRegex =
    Regex("([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})")

data class MermaidLink(
    val range: TextRange,
    val documentId: String
)

private fun PsiElement.shouldSkipPSIElement(): Boolean {
    if (this.language == Language.findLanguageByID("Markdown")) {
        return this is PsiFile
    } else if (this.language == Language.findLanguageByID("textmate")) {
        return this !is PsiFile
    }

    return this.children.isNotEmpty()

    //return this.node != this //Prevent following PSI elements, that captures comment, to be considered as comment
}

fun PsiElement.mermaidLinks(): List<MermaidLink> {
    if (this.shouldSkipPSIElement()) return emptyList()

    val matches = mermaidLinkRegex.findAll(this.text)

    return matches
        .toList()
        .map { match ->
            val range = TextRange(this.textOffset + match.range.first, this.textOffset + match.range.last + 1)
            val id = match.value.substring(15, match.value.length - 1)
            MermaidLink(range, id)
        }
}