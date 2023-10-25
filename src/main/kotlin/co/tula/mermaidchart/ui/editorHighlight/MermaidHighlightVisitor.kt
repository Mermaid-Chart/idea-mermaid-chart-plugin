package co.tula.mermaidchart.ui.editorHighlight

import co.tula.mermaidchart.ui.Icons
import co.tula.mermaidchart.utils.MermaidLink
import co.tula.mermaidchart.utils.mermaidLinks
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import java.awt.Color
import java.awt.Font
import javax.swing.Icon

class MermaidCommentAnnotator : Annotator {

    private val highlightAttributes = TextAttributes(
        null,
        Color(255, 71, 123, 77),
        null,
        EffectType.BOXED,
        Font.PLAIN
    )

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val processedLinks = holder.currentAnnotationSession.getUserData(Keys.PROCESSED_RANGES).orEmpty().toMutableMap()
        val links = element.mermaidLinks()

        links.forEach f@{ link ->
            if (processedLinks.contains(link.key())) {
                return@f
            }

            processedLinks[link.key()] = true

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(link.range)
                .enforcedTextAttributes(highlightAttributes)
                .gutterIconRenderer(SideIconGutterRenderer(link.range.startOffset)).create()
        }

        holder.currentAnnotationSession.putUserData(Keys.PROCESSED_RANGES, processedLinks)
    }

    private object Keys {
        val PROCESSED_RANGES = Key.create<Map<String, Boolean>>("processed_keys")
    }
}

private fun MermaidLink.key(): String = "Link-${range.startOffset}-${range.endOffset}"

private class SideIconGutterRenderer(private val rangeStart: Int) : GutterIconRenderer() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SideIconGutterRenderer) return false

        if (rangeStart != other.rangeStart) return false

        return true
    }

    override fun getAlignment(): Alignment = Alignment.RIGHT

    override fun isDumbAware(): Boolean = true
    override fun hashCode(): Int {
        return rangeStart
    }

    override fun getIcon(): Icon = Icons.Mermaid

}