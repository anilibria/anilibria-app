package ru.radiationx.shared_app.codecs

import ru.radiationx.shared_app.codecs.types.*

fun Codec.isSoftware() = processingType == CodecProcessingType.SOFTWARE
fun Codec.isHardware() = processingType == CodecProcessingType.HARDWARE

fun Codec.isEncoder() = type == CodecType.ENCODER
fun Codec.isDecoder() = type == CodecType.DECODER

fun Codec.isAudio() = outputType == CodecOutputType.AUDIO
fun Codec.isVideo() = outputType == CodecOutputType.VIDEO

fun List<Codec>.filterKnown(knownCodec: KnownCodec) = filter { it.knownCodec == knownCodec }


