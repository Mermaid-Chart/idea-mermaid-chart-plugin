package co.tula.mermaidchart.ui.editorHighlight

import co.tula.mermaidchart.utils.mermaidLinks
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement
import co.tula.mermaidchart.ui.Icons
import java.awt.Color
import java.awt.Font
import javax.swing.Icon
// [MermaidChart: d04fc9a8-213c-4b9f-ac80-bd950faf3767]
class MermaidCommentAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val links = element.mermaidLinks()

        links.forEach { link ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(link.range)
                .enforcedTextAttributes(
                    TextAttributes(
                        null, Color(255, 71, 123, 77),
                        null,
                        EffectType.BOXED,
                        Font.PLAIN
                    )
                )
                .gutterIconRenderer(SideIconGutterRenderer(link.range.startOffset)).create()
        }
    }
}

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