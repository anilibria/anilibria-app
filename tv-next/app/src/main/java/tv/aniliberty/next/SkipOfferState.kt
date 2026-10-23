package tv.aniliberty.next

/** Offers remain available until explicit dismissal or the end of their segment. */
internal class SkipOfferState {
    private val dismissed = mutableSetOf<String>()

    fun dismissed(key: String) = key in dismissed

    fun dismiss(key: String) {
        dismissed += key
    }

    fun visible(key: String?, hidden: Boolean) = hidden && key != null && !dismissed(key)
}

internal data class SkipSegment(
    val key: String,
    val skipLabel: String,
    val watchLabel: String,
    val stop: Long,
)
