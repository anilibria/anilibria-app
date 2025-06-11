package ru.radiationx.anilibria.ui.activities.main

import android.os.Parcelable
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.CollectionsAnalytics
import ru.radiationx.data.analytics.features.FavoritesAnalytics
import ru.radiationx.data.analytics.features.FeedAnalytics
import ru.radiationx.data.analytics.features.OtherAnalytics
import ru.radiationx.data.analytics.features.YoutubeVideosAnalytics
import ru.radiationx.data.api.auth.AuthRepository
import ru.radiationx.data.api.auth.models.AuthState
import ru.radiationx.shared.ktx.EventFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainTabsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val catalogAnalytics: CatalogAnalytics,
    private val collectionsAnalytics: CollectionsAnalytics,
    private val favoritesAnalytics: FavoritesAnalytics,
    private val feedAnalytics: FeedAnalytics,
    private val youtubeVideosAnalytics: YoutubeVideosAnalytics,
    private val otherAnalytics: OtherAnalytics,
    private val systemMessenger: SystemMessenger,
    private val router: Router
) : ViewModel() {

    companion object {
        private val EXIT_TIME = TimeUnit.SECONDS.toMillis(3)
    }

    private var exitToastTime = 0L

    private val _tabsState = MutableStateFlow(createInitialState())
    val tabsState = _tabsState.asStateFlow()

    private val _scrollTopEvent = EventFlow<MainTab>()
    val scrollTopEvent = _scrollTopEvent.observe()

    private val _tabResetEvent = EventFlow<MainTab>()
    val tabResetEvent = _tabResetEvent.observe()

    fun init(initialStack: List<MainTab>?) {
        if (initialStack != null) {
            _tabsState.update {
                it.copy(stack = coerceStack(it.tabs, initialStack))
            }
        }
        authRepository
            .observeAuthState()
            .onEach { authState ->
                val tabs = buildTabs(authState == AuthState.AUTH)
                _tabsState.update {
                    it.copy(
                        tabs = tabs,
                        stack = coerceStack(tabs, it.stack)
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onTabClick(tab: MainTab) {
        if (tabsState.value.selected == tab) {
            _scrollTopEvent.set(tab)
            return
        }
        updateStack { tabsStack ->
            tabsStack.toMutableList().apply {
                remove(tab)
                add(tab)
            }
        }
        submitAnalytics(tab)
    }

    fun onTabLongClick(tab: MainTab) {
        _tabResetEvent.set(tab)
    }

    fun onBackPressed() {
        if (_tabsState.value.stack.isEmpty()) {
            exitWithToast()
        } else {
            updateStack { tabsStack ->
                tabsStack.toMutableList().apply {
                    remove(tabsStack.last())
                }
            }
        }
    }

    private fun exitWithToast() {
        val diff = SystemClock.elapsedRealtime() - exitToastTime
        if (diff > EXIT_TIME) {
            exitToastTime = SystemClock.elapsedRealtime()
            systemMessenger.showMessage("Нажмите кнопку назад снова, чтобы выйти из программы")
        } else {
            router.exit()
        }
    }

    private fun createInitialState(): MainTabsState {
        val authState = runBlocking { authRepository.getAuthState() }
        val tabs = buildTabs(authState == AuthState.AUTH)
        return MainTabsState(tabs, emptyList())
    }

    private fun updateStack(block: (List<MainTab>) -> List<MainTab>) {
        _tabsState.update {
            it.copy(stack = block.invoke(it.stack))
        }
    }

    private fun coerceStack(tabs: List<MainTab>, stack: List<MainTab>): List<MainTab> {
        val allowedTabs = stack.toSet().intersect(tabs.toSet())
        val newStack = mutableListOf<MainTab>()
        stack.forEach {
            if (it in allowedTabs) {
                newStack.add(it)
            }
        }
        return newStack
    }

    private fun buildTabs(hasAuth: Boolean): List<MainTab> = buildList {
        add(MainTab.Feed)
        if (hasAuth) {
            add(MainTab.Favorites)
        }
        add(MainTab.Catalog)
        if (hasAuth) {
            add(MainTab.Collections)
        }
        if (!hasAuth) {
            add(MainTab.YouTube)
        }
        add(MainTab.Other)
    }

    private fun submitAnalytics(tab: MainTab) {
        when (tab) {
            MainTab.Feed -> feedAnalytics.open(AnalyticsConstants.screen_main)
            MainTab.Favorites -> favoritesAnalytics.open(AnalyticsConstants.screen_main)
            MainTab.Catalog -> catalogAnalytics.open(AnalyticsConstants.screen_main)
            MainTab.Collections -> collectionsAnalytics.open(AnalyticsConstants.screen_main)
            MainTab.YouTube -> youtubeVideosAnalytics.open(AnalyticsConstants.screen_main)
            MainTab.Other -> otherAnalytics.open(AnalyticsConstants.screen_main)
        }
    }
}

data class MainTabsState(
    val tabs: List<MainTab>,
    val stack: List<MainTab>,
) {
    val selected: MainTab = stack.lastOrNull() ?: tabs.first()
}

@Parcelize
enum class MainTab(val key: String) : Parcelable {
    Feed("feed"),
    Favorites("favorites"),
    Catalog("search"),
    Collections("collections"),
    YouTube("youtube"),
    Other("other")
}