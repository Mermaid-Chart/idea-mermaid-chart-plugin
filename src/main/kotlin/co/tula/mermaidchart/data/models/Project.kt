package co.tula.mermaidchart.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String,
    val title: String
)
