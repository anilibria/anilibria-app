package tv.aniliberty.next

data class UiState(
    val screen: Screen = Screen.Welcome,
    val loading: Boolean = false,
    val error: String? = null,
    val releases: List<Release> = emptyList(),
    val detail: Release? = null,
    val more: Boolean = false,
    val home: HomeData? = null,
    val homeDay: Int = 1,
    val discovery: Discovery? = null,
    val discoveryLoading: Boolean = false,
    val section: SectionData? = null,
    val refreshing: Set<HomeSection> = emptySet(),
    val refreshErrors: Set<HomeSection> = emptySet(),
    val catalogPage: Int = 1,
    val catalogTotal: Int? = null,
    val loadingMore: Boolean = false,
    val moreError: String? = null,
)
