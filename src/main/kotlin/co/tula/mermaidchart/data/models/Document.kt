package co.tula.mermaidchart.data.models

import co.tula.mermaidchart.data.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class Document(
    @SerialName("title") val title: String?,
    @SerialName("major") val major: Int,
    @SerialName("minor") val minor: Int,
    @SerialName("documentID") val documentId: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("updatedAt")
    val updateTime: OffsetDateTime,
)

