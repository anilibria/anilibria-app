package ru.radiationx.anilibria.contentprovider

import android.app.SearchManager
import android.provider.BaseColumns

/*
* title, duration, productionYear необходимы андроиду
* */
class SystemSuggestionEntity(
    val id: Int,
    val title: String,
    val duration: Int,
    val productionYear: Int,
    val description: String? = null,
    val cardImage: String? = null,
    val backgroundImage: String? = null,
    val videoUrl: String? = null,
    val contentType: String? = null,
    val live: Boolean? = null,
    val width: Int? = null,
    val height: Int? = null,
    val audioChannelConfig: String? = null,
    val purchasePrice: String? = null,
    val rentalPrice: String? = null,
    val ratingStyle: Int? = null,
    val ratingScore: Double? = null
) {

    companion object {
        const val KEY_ID = BaseColumns._ID
        const val KEY_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1
        const val KEY_DESCRIPTION = SearchManager.SUGGEST_COLUMN_TEXT_2
        const val KEY_CARD_IMAGE = SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE
        const val KEY_DATA_TYPE = SearchManager.SUGGEST_COLUMN_CONTENT_TYPE
        const val KEY_IS_LIVE = SearchManager.SUGGEST_COLUMN_IS_LIVE
        const val KEY_VIDEO_WIDTH = SearchManager.SUGGEST_COLUMN_VIDEO_WIDTH
        const val KEY_VIDEO_HEIGHT = SearchManager.SUGGEST_COLUMN_VIDEO_HEIGHT
        const val KEY_AUDIO_CHANNEL_CONFIG = SearchManager.SUGGEST_COLUMN_AUDIO_CHANNEL_CONFIG
        const val KEY_PURCHASE_PRICE = SearchManager.SUGGEST_COLUMN_PURCHASE_PRICE
        const val KEY_RENTAL_PRICE = SearchManager.SUGGEST_COLUMN_RENTAL_PRICE
        const val KEY_RATING_STYLE = SearchManager.SUGGEST_COLUMN_RATING_STYLE
        const val KEY_RATING_SCORE = SearchManager.SUGGEST_COLUMN_RATING_SCORE
        const val KEY_PRODUCTION_YEAR = SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR
        const val KEY_COLUMN_DURATION = SearchManager.SUGGEST_COLUMN_DURATION

        val projection = arrayOf(
            KEY_ID,
            KEY_TITLE,
            KEY_DESCRIPTION,
            KEY_CARD_IMAGE,
            KEY_DATA_TYPE,
            KEY_IS_LIVE,
            KEY_VIDEO_WIDTH,
            KEY_VIDEO_HEIGHT,
            KEY_AUDIO_CHANNEL_CONFIG,
            KEY_PURCHASE_PRICE,
            KEY_RENTAL_PRICE,
            KEY_RATING_STYLE,
            KEY_RATING_SCORE,
            KEY_PRODUCTION_YEAR,
            KEY_COLUMN_DURATION
        )
    }

    private val keysMap = mutableMapOf<String, Any?>()

    init {
        keysMap[KEY_ID] = id
        keysMap[KEY_TITLE] = title
        keysMap[KEY_DESCRIPTION] = description
        keysMap[KEY_CARD_IMAGE] = cardImage
        keysMap[KEY_DATA_TYPE] = contentType
        keysMap[KEY_IS_LIVE] = live
        keysMap[KEY_VIDEO_WIDTH] = width
        keysMap[KEY_VIDEO_HEIGHT] = height
        keysMap[KEY_AUDIO_CHANNEL_CONFIG] = audioChannelConfig
        keysMap[KEY_PURCHASE_PRICE] = purchasePrice
        keysMap[KEY_RENTAL_PRICE] = rentalPrice
        keysMap[KEY_RATING_STYLE] = ratingStyle
        keysMap[KEY_RATING_SCORE] = ratingScore
        keysMap[KEY_PRODUCTION_YEAR] = productionYear
        keysMap[KEY_COLUMN_DURATION] = duration
    }

    fun getRow(): Array<Any?> = projection.map { keysMap[it] }.toTypedArray()
}