package ru.radiationx.shared_app.codecs

import ru.radiationx.shared_app.codecs.types.Codec
import ru.radiationx.shared_app.codecs.types.CodecQuery
import ru.radiationx.shared_app.codecs.types.KnownCodec

object MediaCodecsFinder {

    private val nameRegex by lazy {
        Regex(
            "(?:\\w+\\.\\w+\\.([\\w\\.]+?)\\.(?:decoder|encoder)(?:\\.(\\w+))?)",
            RegexOption.IGNORE_CASE
        )
    }

    private val typeRegex by lazy {
        Regex("(?:audio|video)\\/(.+)")
    }

    private val codecsMap = mapOf(
        CodecQuery("aac", "mp4") to KnownCodec.AUDIO_AAC,
        CodecQuery("mp3", "mpeg") to KnownCodec.AUDIO_OPUS,
        CodecQuery("opus", "opus") to KnownCodec.AUDIO_MP3,
        CodecQuery("vorbis", "vorbis") to KnownCodec.AUDIO_VORBIS,

        //Same codec avc/h264
        CodecQuery("avc", "avc") to KnownCodec.VIDEO_AVC,
        CodecQuery("h264", "avc") to KnownCodec.VIDEO_AVC,

        CodecQuery("hevc", "hevc") to KnownCodec.VIDEO_HEVC,
        CodecQuery("av1", "av01") to KnownCodec.VIDEO_AV1,
        CodecQuery("vp9", "vp9") to KnownCodec.VIDEO_VP9
    )

    fun find(query: CodecQuery): List<Codec> {
        return MediaCodecsProvider.codecs.filter {
            check(it.name, it.mimeTypes, query)
        }
    }

    fun getKnown(name: String, mimeTypes: List<String>): KnownCodec? {
        return codecsMap.firstNotNullOfOrNull { (query, knownType) ->
            knownType.takeIf { check(name, mimeTypes, query) }
        }
    }

    private fun check(codecName: String, codecMimeTypes: List<String>, query: CodecQuery): Boolean {
        val codecNameResult = nameRegex.find(codecName)?.let { matchResult ->
            val name = matchResult.groups[1]?.value
            val endName = matchResult.groups[2]?.value
            endName ?: name
        }
        val typeNamesResult = codecMimeTypes.map { type ->
            typeRegex.find(type)?.let { matchResult ->
                matchResult.groups[1]?.value
            }
        }
        val isNameContains = codecNameResult?.contains(query.name, true) ?: false
        val isTypeContains = typeNamesResult.any { it?.contains(query.type, true) ?: false }
        return isNameContains && isTypeContains
    }
}