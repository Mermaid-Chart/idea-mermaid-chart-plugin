package co.tula.mermaidchart.settings

import co.tula.mermaidchart.utils.MessageProvider.message
import com.intellij.codeInsight.codeVision.settings.CodeVisionGroupSettingProvider

class MermaidCodeVisionSettingsProvider: CodeVisionGroupSettingProvider {
    override val groupId: String
        get() = message("codeVision.group.id")

    override val description: String
        get() = message("codeVision.group.description")

    override val groupName: String
        get() = message("codeVision.group.title")
}