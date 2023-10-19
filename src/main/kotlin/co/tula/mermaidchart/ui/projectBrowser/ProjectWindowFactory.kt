package co.tula.mermaidchart.ui.projectBrowser

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.JComponent

class ProjectWindowFactory: ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val panel = ProjectBrowserPanel(project)
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)

    }
}