package co.tula.mermaidchart.ui.projectBrowser

import co.tula.mermaidchart.data.models.ProjectWithDocuments
import co.tula.mermaidchart.settings.MermaidSettingsConfigurable
import co.tula.mermaidchart.settings.MermaidSettingsListener
import co.tula.mermaidchart.settings.MermaidSettingsTopic
import co.tula.mermaidchart.utils.CommentUtils
import co.tula.mermaidchart.utils.Left
import co.tula.mermaidchart.utils.MessageProvider.message
import co.tula.mermaidchart.utils.Right
import co.tula.mermaidchart.utils.extensions.isUnauthorized
import co.tula.mermaidchart.utils.extensions.withApi
import co.tula.mermaidchart.utils.notifyError
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.LoadingNode
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.io.HttpRequests
import kotlinx.coroutines.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeModel

class ProjectBrowserPanel(
    private val project: Project
) : SimpleToolWindowPanel(true) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var browser: Tree? = null
    private var refreshJob: Job? = null

    init {
        val browser = Tree(buildLoadingTree())

        this.browser = browser

        val clickListener = TreeClickListener(
            browser,
            ::onChartClick,
            onRefreshClick = { refresh(true) },
            onSettingClick = { openSettings(project) }
        )

        browser.addMouseListener(clickListener)

        setToolbar(
            buildToolbar(
                onRefresh = { refresh(true) }
            )
        )
        setContent(browser)

        ApplicationManager.getApplication()
            .messageBus
            .connect()
            .subscribe(MermaidSettingsTopic.TOPIC, MermaidSettingsListener { refresh() })

        refresh()
    }

    private fun buildToolbar(onRefresh: () -> Unit): ActionToolbarImpl {
        val actions = object : ActionGroup() {
            override fun getChildren(e: AnActionEvent?): Array<AnAction> {
                return arrayOf(RefreshAction(onRefresh), SettingsAction(project))
            }
        }
        return ActionToolbarImpl(ActionPlaces.TOOLBAR, actions, true).apply {
            targetComponent = this
        }
    }

    private fun rootNode(): DefaultMutableTreeNode = DefaultMutableTreeNode(message("projects.list.project"))

    private fun buildTree(data: List<ProjectWithDocuments>): TreeModel {
        val rootNode = rootNode()

        data
            .map { (project, documents) ->
                val node = DefaultMutableTreeNode(project.title)
                documents
                    .map { DocumentTreeNode(it.title ?: message("projects.list.untitled"), it.documentId) }
                    .forEach { node.add(it) }
                node
            }
            .forEach { rootNode.add(it) }

        return DefaultTreeModel(rootNode)
    }

    private fun buildLoadingTree(): TreeModel {
        val projects = rootNode()
        val loadingNode = LoadingNode(message("projects.list.loading"))

        projects.add(loadingNode)

        return DefaultTreeModel(projects)
    }

    private fun buildFailedTree(exception: Exception): TreeModel {
        val projects = rootNode()
        val errorNode = NetworkErrorTreeNode(project, exception)

        projects.add(errorNode)

        return DefaultTreeModel(projects)
    }

    private fun updateBrowserModel(model: TreeModel) {
        SwingUtilities.invokeLater {
            browser?.model = model
        }
    }

    private fun refresh(withDelay: Boolean = false) {
        updateBrowserModel(buildLoadingTree())
        refreshJob?.cancel()
        refreshJob = scope.launch {
            if (withDelay) {
                delay(500)
            }
            project.withApi { api ->
                when (val projects = api.projectsWithDocuments()) {
                    is Left -> {
                        project.notifyError(projects.v.localizedMessage, projects.v)
                        updateBrowserModel(buildFailedTree(projects.v))
                    }

                    is Right -> updateBrowserModel(buildTree(projects.v))
                }
            }
        }
    }

    private fun onChartClick(chartId: String) {
        val editor = (FileEditorManager.getInstance(project).selectedEditor as? TextEditor)?.editor
            ?: return

        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)

        val link = CommentUtils.wrapInComment(psiFile, editor, "[MermaidChart: $chartId]")

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

    private class NetworkErrorTreeNode(
        private val project: Project,
        val exception: Exception
    ) : DefaultMutableTreeNode() {
        override fun getUserObject(): Any {
            return object : PresentableNodeDescriptor<NetworkErrorTreeNode>(project, null) {
                init {
                    val defaultTextColor = EditorColorsManager.getInstance()
                        .schemeForCurrentUITheme
                        .getAttributes(HighlighterColors.TEXT)
                    val linkTextColor = EditorColorsManager.getInstance()
                        .schemeForCurrentUITheme
                        .getAttributes(EditorColors.REFERENCE_HYPERLINK_COLOR)


                    val (description, link) = if (exception is HttpRequests.HttpStatusException && exception.isUnauthorized) {
                        message("projects.list.error.unauthorized") to message("projects.list.openSettings")
                    } else {
                        message("projects.list.error.common") to message("projects.list.refresh")
                    }
                    presentation.addText(
                        ColoredFragment(
                            "$description ",
                            SimpleTextAttributes.fromTextAttributes(defaultTextColor)
                        )
                    )
                    presentation.addText(
                        ColoredFragment(
                            link,
                            SimpleTextAttributes.fromTextAttributes(linkTextColor)
                        )
                    )
                }

                override fun update(presentation: PresentationData) {}
                override fun getElement(): NetworkErrorTreeNode = this@NetworkErrorTreeNode

            }
        }
    }

    private class TreeClickListener(
        private val rootTree: Tree,
        private val onDocumentClick: (documentId: String) -> Unit,
        private val onRefreshClick: () -> Unit,
        private val onSettingClick: () -> Unit,
    ) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            super.mouseClicked(e)

            val node = rootTree.lastSelectedPathComponent
            if (node is DocumentTreeNode) {
                onDocumentClick(node.documentId)
            } else if (node is NetworkErrorTreeNode) {
                if (node.exception is HttpRequests.HttpStatusException && node.exception.isUnauthorized) {
                    onSettingClick()
                } else {
                    onRefreshClick()
                }
            }
        }
    }
}

private class SettingsAction(private val project: Project) :
    AnAction(message("actions.settings.title"), null, AllIcons.General.GearPlain) {

    override fun actionPerformed(e: AnActionEvent) {
        openSettings(project)
    }
}

private class RefreshAction(private val onClick: () -> Unit) :
    AnAction(message("actions.refresh.title"), null, AllIcons.Actions.Refresh) {

    override fun actionPerformed(e: AnActionEvent) {
        onClick()
    }
}

private fun openSettings(project: Project) {
    ShowSettingsUtil.getInstance().showSettingsDialog(project, MermaidSettingsConfigurable::class.java)
}