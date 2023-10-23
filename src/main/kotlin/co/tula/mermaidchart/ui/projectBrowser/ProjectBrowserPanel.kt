package co.tula.mermaidchart.ui.projectBrowser

import co.tula.mermaidchart.settings.MermaidSettingsConfigurable
import co.tula.mermaidchart.utils.CommentUtils
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiManager
import com.intellij.ui.treeStructure.Tree
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode

class ProjectBrowserPanel(
    private val project: Project
) : SimpleToolWindowPanel(true) {
    init {

        val toolbar = buildToolbar()
        val browser = Tree(buildTree())

        val clickListener = DocumentClickListener(browser, ::onChartClick)

        browser.addMouseListener(clickListener)

        setToolbar(toolbar)
        setContent(browser)
    }

    private fun buildToolbar(): ActionToolbarImpl {
        val actions = object : ActionGroup() {
            override fun getChildren(e: AnActionEvent?): Array<AnAction> {
                return arrayOf(SettingsAction(project))
            }
        }
        return ActionToolbarImpl(ActionPlaces.TOOLBAR, actions, true)
    }

    private fun buildTree(): TreeNode {
        val projects = DefaultMutableTreeNode("Projects")
        val mockedProject = DefaultMutableTreeNode("Mocked Project")
        val mockedDocument = DocumentTreeNode("Mocked Doc", "5089868e-68e3-45cf-982a-21129803cb19")

        mockedProject.add(mockedDocument)
        projects.add(mockedProject)

        return projects
    }

    private fun onChartClick(chartId: String) {
        val editor = (FileEditorManager.getInstance(project).selectedEditor as? TextEditor)?.editor
            ?: return

        val psiFile = PsiManager.getInstance(project).findFile(editor.virtualFile)

        val link = "${CommentUtils.getCommentPrefix(psiFile, editor)} [MermaidChart: $chartId]"

        val document = editor.document

        val currentLine = document.getLineNumber(editor.caretModel.offset)
        val currentLineRange =
            TextRange(document.getLineStartOffset(currentLine), document.getLineEndOffset(currentLine))

        fun insert(offset: Int, text: String) {
            runUndoTransparentWriteAction {
                document.insertString(offset, text)
                editor.caretModel.moveToOffset(offset + text.length)
                IdeFocusManager.getInstance(project).requestFocus(editor.contentComponent, true)
            }
        }

        if (document.getText(currentLineRange).isEmpty()) {
            insert(document.getLineStartOffset(currentLine), link)
        } else if (currentLine == 0) {
            insert(0, "$link\n")
        } else {
            val previousLineStart = document.getLineStartOffset(currentLine) - 1
            insert(previousLineStart, "\n$link")
        }
    }

    private class DocumentTreeNode(
        name: String,
        val documentId: String
    ) : DefaultMutableTreeNode(name)

    private class DocumentClickListener(
        private val rootTree: Tree,
        private val onClick: (documentId: String) -> Unit
    ) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            super.mouseClicked(e)

            val node = rootTree.lastSelectedPathComponent
            if (node is DocumentTreeNode) {
                onClick(node.documentId)
            }
        }
    }
}

private class SettingsAction(private val project: Project) : AnAction() {
    init {
        templatePresentation.apply {
            icon = AllIcons.General.GearPlain
            text = "Settings"
            isEnabled = true
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, MermaidSettingsConfigurable::class.java)
    }
}