package co.tula.mermaidchart.utils

import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.LanguageCommenters
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.impl.AbstractFileType
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider
import com.intellij.psi.util.PsiUtilBase

object CommentUtils {
    fun getCommentPrefix(file: PsiFile?, editor: Editor): String {
        val commenter = file?.let { findCommenter(file, editor) }
        return commenter?.lineCommentPrefix ?: "//"
    }

    //Fork of CommentByBlockCommentHandler.java
    private fun findCommenter(file: PsiFile, editor: Editor): Commenter? {
        val fileType = file.fileType
        if (fileType is AbstractFileType) return fileType.commenter
        val lang = PsiUtilBase.getLanguageInEditor(editor.caretModel.currentCaret, file.project)
        return getCommenter(file, editor, lang, lang)
    }

    private fun getCommenter(
        file: PsiFile,
        editor: Editor?,
        lineStartLanguage: Language?,
        lineEndLanguage: Language?
    ): Commenter? {
        val viewProvider = file.viewProvider
        for (provider in MultipleLangCommentProvider.EP_NAME.extensions) {
            if (provider.canProcess(file, viewProvider)) {
                return provider.getLineCommenter(file, editor, lineStartLanguage, lineEndLanguage)
            }
        }
        val fileLanguage = file.language
        var lang = if (lineStartLanguage == null ||
            LanguageCommenters.INSTANCE.forLanguage(lineStartLanguage) == null ||
            fileLanguage.baseLanguage === lineStartLanguage // file language is a more specific dialect of the line language
        )
            fileLanguage
        else
            lineStartLanguage

        if (viewProvider is TemplateLanguageFileViewProvider && lang === viewProvider.templateDataLanguage) {
            lang = viewProvider.getBaseLanguage()
        }

        return LanguageCommenters.INSTANCE.forLanguage(lang)
    }
}