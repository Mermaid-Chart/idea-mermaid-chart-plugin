package com.example.mermaidchart.ui.editorHighlight

import com.example.mermaidchart.utils.isMermaidLink
import com.example.mermaidchart.utils.mermaidLinkRange
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import java.awt.Color
import java.awt.Font

class MermaidCommentAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!element.isMermaidLink()) return

        val linkRange = element.mermaidLinkRange() ?: return
        //Skip "//" for highlight
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(linkRange)
            .enforcedTextAttributes(TextAttributes(null, Color(255, 71, 123, 77), null, EffectType.BOXED, Font.PLAIN))
            .create()
    }
}