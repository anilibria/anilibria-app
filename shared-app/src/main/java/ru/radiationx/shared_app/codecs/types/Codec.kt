package ru.radiationx.shared_app.codecs.types

data class Codec(
    val name: String,
    val mimeTypes: List<String>,
    val type: CodecType,
    val outputType: CodecOutputType,
    val processingType: CodecProcessingType,
    val knownCodec: KnownCodec?
)

