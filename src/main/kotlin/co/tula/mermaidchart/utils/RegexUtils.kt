package co.tula.mermaidchart.utils

import ai.grazie.utils.findAllMatches
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private val mermaidLinkRegex =
    Regex("\\[MermaidChart: ([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})]")

@OptIn(ExperimentalContracts::class)
fun PsiElement.isMermaidLink(): Boolean {
    contract {
        returns(true) implies (this@isMermaidLink is PsiComment)
    }
    return this is PsiComment && mermaidLinkRegex.containsMatchIn(this.text)
}

fun PsiElement.mermaidLinkRange(): TextRange? {
    if(!isMermaidLink()) return null
    val match = mermaidLinkRegex.findAllMatches(this.text).firstOrNull() ?: return null

    return TextRange(this.textOffset + match.range.first, this.textOffset + match.range.last + 1)
}