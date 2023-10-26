package co.tula.mermaidchart.utils

import com.intellij.DynamicBundle


object MessageProvider : DynamicBundle("i18n.messages") {
    fun message(key: String, vararg args: Any): String = messageOrDefault(key, key, *args)
}