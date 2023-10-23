package co.tula.mermaidchart.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Document(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("major") val major: Int,
    @SerialName("minor") val minor: Int,
    @SerialName("documentID") val documentId: String
)
