package co.tula.mermaidchart.utils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

private val mermaidLinkRegex =
    Regex("\\[MermaidChart: ([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})]")

fun PsiElement.mermaidLinkRange(): List<TextRange> {
    if (this != this.node) return emptyList() //Prevent following PSI elements, that captures comment, to be considered as comment

    val matches = mermaidLinkRegex.findAll(this.text)

    return matches
        .toList()
        .map { match -> TextRange(this.textOffset + match.range.first, this.textOffset + match.range.last + 1) }
}