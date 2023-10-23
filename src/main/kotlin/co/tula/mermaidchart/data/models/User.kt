package co.tula.mermaidchart.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val fullName: String
)
