package co.tula.mermaidchart.settings

import com.intellij.codeInsight.codeVision.settings.CodeVisionGroupSettingProvider

class MermaidCodeVisionSettingsProvider: CodeVisionGroupSettingProvider {
    override val groupId: String
        get() = "mermaidcharts"

    override val description: String
        get() = "View / Edit action buttons"

    override val groupName: String
        get() = "Mermaid Charts"
}