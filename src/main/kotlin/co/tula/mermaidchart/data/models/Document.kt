package co.tula.mermaidchart.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val id: String,
    val title: String,
    val major: Int,
    val minor: Int
)
