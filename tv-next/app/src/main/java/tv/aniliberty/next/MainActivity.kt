package tv.aniliberty.next

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit

class MainActivity : ComponentActivity() {
    private val remoteRepeat = RemoteRepeat()
    private val remoteClick = RemoteClick()
    private var shelfMoveToken = 0
    private var pendingShelf: Pair<RecyclerView, String>? = null
    private var remotePendingKey: String? = null
    private val model by viewModels<TvModel>()
    private lateinit var list: RecyclerView
    private lateinit var adapter: PageAdapter
    private lateinit var grid: GridLayoutManager
    private val prefs
        get() = playbackPreferences(this)

    private val settings by lazy { TvSettings(this) }
    private val account by lazy { AccountRepository(this) }
    private var accountJob: kotlinx.coroutines.Job? = null
    private var accountLoadedAt = 0L
    private var accountHistoryJob: kotlinx.coroutines.Job? = null
    private val history
        get() = ViewingHistory(prefs)

    private var historyHydrating = false
    private var lastHistoryHydration = 0L
    private var resumedOnce = false
    private val childFocusMemory = mutableMapOf<Pair<Screen, String>, Int>()
    private var remoteFocusToken = 0
    private val focusMemory = mutableMapOf<Screen, String>()
    private val scrollMemory = mutableMapOf<Screen, Pair<String, Int>>()

    private fun screenKey(screen: Screen) =
        if (screen is Screen.Detail) screen.copy(tab = 0) else screen

    private fun focusItem(item: View?) {
        adapter.cancelPendingShelfFocus()
        when (item) {
            is RecyclerView -> adapter.restoreShelf(item)
            is android.widget.HorizontalScrollView -> {
                val line = item.getChildAt(0) as? LinearLayout
                if (current == Screen.Home && item.tag == "schedule-days")
                    line?.getChildAt(model.state.value.homeDay + 1)?.requestFocus()
                else {
                    val choices = line?.getFocusables(View.FOCUS_FORWARD).orEmpty()
                    val row = adapter.rows.firstOrNull { it.key == item.tag }
                    val saved =
                        childFocusMemory[screenKey(current) to (item.tag as? String).orEmpty()]
                    val target =
                        if (row?.key == "account-collections-tabs") row.selected
                        else saved ?: if (row?.kind == Kind.ACCOUNT_TABS) row.selected else 0
                    choices.getOrNull(target)?.requestFocus()
                        ?: choices.firstOrNull()?.requestFocus()
                }
            }
            null -> Unit
            else ->
                if (item.isFocusable) item.requestFocus()
                else if (item is ViewGroup)
                    item.getFocusables(View.FOCUS_FORWARD).firstOrNull()?.requestFocus()
        }
    }

    private lateinit var startup: StartupScreen
    private var current: Screen = Screen.Welcome
    private val detailBookmarks = linkedMapOf<Pair<Int, Int>, DetailBookmark>()
    private val detailContentBookmarks = linkedMapOf<Pair<Int, Int>, DetailBookmark>()
    private val detailHeroButtons = linkedMapOf<Int, String>()
    private var detailFocusToken = 0
    private var detailFocusPending = false

    private fun detailAddress(): DetailBookmark? {
        val focus = list.findFocus() ?: return null
        val row = list.findContainingItemView(focus) ?: return null
        return DetailBookmark(
            row.tag as? String ?: return null,
            (focus.tag as? String)?.takeIf { it != row.tag },
        )
    }

    private fun rememberDetailFocus() {
        val screen = current as? Screen.Detail ?: return
        val address = detailAddress() ?: return
        val key = screen.id to screen.tab
        if (detailBookmarks.size > 48) {
            detailBookmarks.remove(detailBookmarks.keys.first())
            detailContentBookmarks.keys.firstOrNull()?.let { detailContentBookmarks.remove(it) }
        }
        detailBookmarks[key] = address
        if (address.row == "hero") address.child?.let { detailHeroButtons[screen.id] = it }
        if (address.row != "hero" && address.row != "tabs") detailContentBookmarks[key] = address
        (list.findViewWithTag<View>(address.row) as? RecyclerView)?.let {
            adapter.rememberShelf(it)
        }
    }

    private fun focusDetail(index: Int, child: String? = null, top: Boolean = false) {
        val screen = current as? Screen.Detail ?: return
        if (index !in adapter.rows.indices) return
        val key = adapter.rows[index].key
        val token = ++detailFocusToken
        fun apply() {
            if (
                token != detailFocusToken || current != screen || menuOpen || !list.hasWindowFocus()
            )
                return
            val item = list.findViewHolderForAdapterPosition(index)?.itemView ?: return
            if (item.tag != key || (key == "hero" && item.getTag(R.id.hero_release) != screen.id))
                return
            val exact = child?.let { item.findViewWithTag<View>(it) }
            if (exact?.isFocusable == true) exact.requestFocus()
            else if (key == "tabs") {
                item.findViewWithTag<View>("detail-tab:${screen.tab}")?.requestFocus()
            } else focusItem(item)
            detailFocusPending = false
        }
        val item = list.findViewHolderForAdapterPosition(index)?.itemView
        if (
            !top &&
                item != null &&
                (key != "hero" || item.getTag(R.id.hero_release) == screen.id) &&
                item.top >= list.paddingTop &&
                item.bottom <= list.height - list.paddingBottom
        ) {
            apply()
            return
        }
        detailFocusPending = true
        list.stopScroll()
        list.doOnNextLayout {
            if (token == detailFocusToken) {
                detailFocusPending = false
                apply()
            }
        }
        grid.scrollToPositionWithOffset(
            index,
            dp(if (key == "hero") 0 else if (key == "tabs") 12 else 64),
        )
        list.requestLayout()
    }

    private fun focusDetailTab(index: Int = (current as? Screen.Detail)?.tab ?: 0) {
        if (adapter.tabStart >= 0) focusDetail(adapter.tabStart, "detail-tab:$index")
    }

    private fun enterDetailContent() {
        val screen = current as? Screen.Detail ?: return
        val bookmark = detailContentBookmarks[screen.id to screen.tab]
        val position =
            adapter.rows
                .indexOfFirst { it.key == bookmark?.row }
                .takeIf { it >= adapter.contentStart && it >= 0 }
                ?: (adapter.contentStart.coerceAtLeast(0) until adapter.rows.size).firstOrNull {
                    adapter.rows[it].kind != Kind.HEADING
                }
        if (position != null) focusDetail(position, bookmark?.child)
    }

    @android.annotation.SuppressLint(
        "RestrictedApi"
    ) // Activity delegates TV key events through its inherited dispatcher.
    private fun detailKey(event: KeyEvent): Boolean {
        val screen = current as? Screen.Detail ?: return false
        if (
            menuOpen ||
                event.keyCode !in
                    listOf(
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_DPAD_RIGHT,
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN,
                    )
        )
            return false
        if (event.action != KeyEvent.ACTION_DOWN) return true
        if (detailFocusPending) return true
        val focus = list.findFocus() ?: return false
        val outer = list.findContainingItemView(focus) ?: return false
        val position = list.getChildAdapterPosition(outer)
        val row = adapter.rows.getOrNull(position) ?: return false
        rememberDetailFocus()
        val horizontal =
            event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
        val forward =
            event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
        val direction =
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> View.FOCUS_LEFT
                KeyEvent.KEYCODE_DPAD_RIGHT -> View.FOCUS_RIGHT
                KeyEvent.KEYCODE_DPAD_UP -> View.FOCUS_UP
                else -> View.FOCUS_DOWN
            }
        if (row.kind == Kind.HERO || row.kind == Kind.TABS) {
            if (horizontal) {
                val choices =
                    (outer as ViewGroup).getFocusables(View.FOCUS_FORWARD).filter {
                        it is android.widget.TextView && it.isClickable
                    }
                val index = choices.indexOf(focus)
                val target = choices.getOrNull(index + if (forward) 1 else -1)
                if (index >= 0 && target != null) target.requestFocus()
                else if (!forward && index == 0 && event.repeatCount == 0) openMenu()
            } else if (row.kind == Kind.HERO) {
                if (forward) focusDetailTab()
            } else if (forward) {
                val tab =
                    (focus.tag as? String)?.removePrefix("detail-tab:")?.toIntOrNull() ?: screen.tab
                enterSelectedTab(tab)
            } else {
                val hero = adapter.rows.indexOfFirst { it.kind == Kind.HERO }
                if (hero >= 0) focusDetail(hero, detailHeroButtons[screen.id], top = true)
            }
            return true
        }
        if (horizontal && outer is RecyclerView) {
            adapter.rememberShelf(outer)
            val nested = focus.let { outer.findContainingItemView(it) }
            val index = nested?.let { outer.getChildAdapterPosition(it) } ?: -1
            val size = outer.adapter?.itemCount ?: 0
            if (!forward && index == 0) {
                if (event.repeatCount == 0) openMenu()
                return true
            }
            if (forward && index >= size - 1) return true
            super.dispatchKeyEvent(event)
            return true // Keep horizontal navigation inside the nested shelf.
        }
        val target = detailGridTarget(adapter.rows, position, direction)
        if (target != null) {
            focusDetail(target)
            return true
        }
        if (horizontal) {
            if (!forward && event.repeatCount == 0) openMenu()
            return true
        }
        val candidates =
            if (forward) (position + 1 until adapter.rows.size)
            else (position - 1 downTo adapter.contentStart.coerceAtLeast(0))
        val next =
            candidates.firstOrNull {
                adapter.rows[it].kind !in listOf(Kind.HEADING, Kind.TABS, Kind.HERO)
            }
        // Grid top/bottom edges do not wrap to the previous/next row through FocusFinder.
        if (row.kind in listOf(Kind.EPISODE, Kind.POSTER)) {
            if (!forward) focusDetailTab()
        } else if (next != null) focusDetail(next) else if (!forward) focusDetailTab()
        return true
    }

    private var enterContent = false
    private var enterDay = false
    private lateinit var root: android.widget.FrameLayout
    private lateinit var shell: LinearLayout
    private lateinit var menu: LinearLayout

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private var menuOpen = false
    private var contentFocus: View? = null
    private var selectedSection = 0

    private fun buildMenu() {
        menu.removeAllViews()
        menu.layoutParams =
            if (::root.isInitialized && menu.parent === root)
                android.widget.FrameLayout.LayoutParams(dp(if (menuOpen) 196 else 60), -1)
            else LinearLayout.LayoutParams(dp(if (menuOpen) 196 else 60), -1)
        menu.addView(
            LinearLayout(this).apply {
                gravity = android.view.Gravity.CENTER_VERTICAL
                addView(
                    android.widget.ImageView(this@MainActivity).apply {
                        setImageResource(R.drawable.ic_aniliberty)
                        contentDescription = "AniLiberty"
                    },
                    LinearLayout.LayoutParams(dp(36), dp(36)).apply {
                        marginStart = dp(8)
                        marginEnd = dp(10)
                    },
                )
                if (menuOpen)
                    addView(
                        android.widget.TextView(this@MainActivity).apply {
                            label("AniLiberty", 18f, WHITE, true)
                        }
                    )
            },
            LinearLayout.LayoutParams(-1, dp(56)),
        )
        val routes = buildList {
            if (settings.enabled("menu-search")) add(Triple(2, "Поиск", R.drawable.ic_search))
            add(Triple(0, "Главная", R.drawable.ic_home))
            add(Triple(1, "Релизы", R.drawable.ic_catalog))
            add(Triple(9, "Расписание", R.drawable.ic_episodes))
            if (settings.enabled("menu-favorites"))
                add(Triple(4, "Избранное", R.drawable.ic_favorite))
            if (settings.enabled("menu-collections"))
                add(Triple(5, "Коллекции", R.drawable.ic_library))
            add(Triple(6, "История", R.drawable.ic_collection_history))
            add(Triple(3, "Профиль", R.drawable.ic_account))
            if (settings.enabled("menu-random", false))
                add(Triple(8, "Случайный релиз", R.drawable.ic_refresh))
            if (settings.enabled("menu-settings"))
                add(Triple(7, "Настройки", R.drawable.ic_settings))
        }
        val buttons =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            }
        menu.addView(
            android.widget.ScrollView(this).apply {
                isFillViewport = false
                isFocusable = false
                descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                addView(buttons)
            },
            LinearLayout.LayoutParams(-1, 0, 1f),
        )
        routes.forEach { (route, title, icon) ->
            buttons.addView(
                action(buttons, if (menuOpen) title else "", icon = icon) {
                        selectedSection = route
                        closeMenu(false)
                        navigate(route)
                    }
                    .apply {
                        tag = "menu-route:$route"
                        contentDescription = title
                        isFocusableInTouchMode = true
                        if (route == selectedSection) setTextColor(WHITE)
                        setOnKeyListener { _, key, event ->
                            if (key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                                if (event.action == KeyEvent.ACTION_UP) closeMenu()
                                true
                            } else false
                        }
                    },
                LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) },
            )
        }
    }

    private fun homeMenuLayout() {
        if (current != Screen.Welcome && menu.parent === shell) {
            shell.removeView(menu)
            shell.setPadding(dp(60), 0, 0, 0)
            root.addView(
                menu,
                android.widget.FrameLayout.LayoutParams(dp(if (menuOpen) 196 else 60), -1),
            )
        } else if (current == Screen.Welcome && menu.parent === root) {
            root.removeView(menu)
            shell.setPadding(0, 0, 0, 0)
            shell.addView(menu, 0, LinearLayout.LayoutParams(dp(if (menuOpen) 196 else 60), -1))
        }
    }

    private fun openMenu() {
        if (current == Screen.Welcome) return
        remoteClick.cancel()
        shelfMoveToken++
        pendingShelf = null
        remotePendingKey = null
        adapter.cancelPendingShelfFocus()
        remoteFocusToken++
        detailFocusToken++
        detailFocusPending = false
        homeTransition++
        homeFocusTarget = null
        list.stopScroll()
        contentFocus = list.findFocus()
        rememberFocus()
        menuOpen = true
        buildMenu()
        fun focusMenu() {
            if (menuOpen)
                (menu.findViewWithTag<View>("menu-route:$selectedSection")
                        ?: menu.findViewWithTag<View>("menu-route:0"))
                    ?.requestFocus()
        }
        focusMenu()
        menu.doOnNextLayout { menu.post { focusMenu() } }
        menu.requestLayout()
    }

    private fun closeMenu(restore: Boolean = true) {
        menuOpen = false
        buildMenu()
        if (restore) {
            if (contentFocus?.isAttachedToWindow == true) contentFocus?.requestFocus()
            else {
                val key = focusMemory[screenKey(current)]
                val index = adapter.rows.indexOfFirst { it.key == key }
                if (index >= 0) {
                    grid.scrollToPositionWithOffset(index, dp(16))
                    list.doOnNextLayout {
                        if (!menuOpen)
                            focusItem(list.findViewHolderForAdapterPosition(index)?.itemView)
                    }
                    list.requestLayout()
                } else list.requestFocus()
            }
        }
    }

    private var homeFocusTarget: Int? = null
    private var lastHomeMove = 0L
    private var homeTransition = 0

    private fun moveHomeFocus(direction: Int): Boolean {
        if (homeFocusTarget != null) {
            homeTransition++
            homeFocusTarget = null
            list.stopScroll()
        }
        val focus = list.findFocus() ?: return false
        val outer = list.findContainingItemView(focus) ?: return false
        val index = list.getChildAdapterPosition(outer)
        if (outer is RecyclerView) adapter.rememberShelf(outer)
        val target = homeFocusDestination(adapter.rows, index, direction) ?: return true
        val key = adapter.rows[target].key
        val header =
            if (adapter.rows[target].kind == Kind.SHELF)
                homeSectionHeader(adapter.rows, target) ?: target
            else target
        val token = ++homeTransition
        homeFocusTarget = target
        val attachedTarget = list.findViewHolderForAdapterPosition(target)?.itemView
        if (attachedTarget != null) {
            focusItem(attachedTarget)
            homeFocusTarget = null
        }
        fun finish() {
            if (token != homeTransition) return
            homeFocusTarget = null
            if (current != Screen.Home || menuOpen || attachedTarget != null) return
            val resolved = adapter.rows.indexOfFirst { it.key == key }
            if (resolved >= 0) focusItem(list.findViewHolderForAdapterPosition(resolved)?.itemView)
        }
        val heading = grid.findViewByPosition(header)
        val card = grid.findViewByPosition(target)
        val delta =
            if (heading != null && card != null)
                homeScrollDelta(
                    heading.top,
                    card.bottom,
                    list.paddingTop + dp(6),
                    list.height - list.paddingBottom,
                )
            else null
        if (delta == 0) {
            finish()
            return true
        }
        // One scroll owner; attached content receives focus before the animation ends.
        list.stopScroll()
        val listener =
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(view: RecyclerView, state: Int) {
                    if (state != RecyclerView.SCROLL_STATE_IDLE) return
                    list.removeOnScrollListener(this)
                    finish()
                }
            }
        list.addOnScrollListener(listener)
        if (delta != null)
            list.smoothScrollBy(0, delta, android.view.animation.DecelerateInterpolator(), 180)
        else
            grid.startSmoothScroll(
                object : androidx.recyclerview.widget.LinearSmoothScroller(this) {
                        override fun getVerticalSnapPreference() = SNAP_TO_START

                        override fun calculateTimeForDeceleration(dx: Int) = 180
                    }
                    .apply { targetPosition = header }
            )
        return true
    }

    private fun shelfRemoteKey(event: KeyEvent): Boolean {
        if (
            menuOpen ||
                event.keyCode !in listOf(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT)
        )
            return false
        val focus = list.findFocus() ?: return false
        val shelf = list.findContainingItemView(focus) as? RecyclerView ?: return false
        if (event.action != KeyEvent.ACTION_DOWN) return true
        val childAdapter = shelf.adapter as? PageAdapter ?: return true
        adapter.rememberShelf(shelf)
        val saved = pendingShelf?.takeIf { it.first === shelf }?.second
        val actual =
            shelf.findContainingItemView(focus)?.let { shelf.getChildAdapterPosition(it) } ?: -1
        val index = childAdapter.rows.indexOfFirst { it.key == saved }.takeIf { it >= 0 } ?: actual
        if (index < 0) return true
        val step = if (event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) 1 else -1
        val target = index + step
        if (target < 0) {
            if (event.repeatCount == 0) openMenu()
            return true
        }
        val key = childAdapter.rows.getOrNull(target)?.key ?: return true
        val token = ++shelfMoveToken
        val screen = current
        pendingShelf = shelf to key
        fun apply() {
            if (
                token == shelfMoveToken &&
                    current == screen &&
                    !menuOpen &&
                    shelf.adapter === childAdapter
            ) {
                val position = childAdapter.rows.indexOfFirst { it.key == key }
                shelf.findViewHolderForAdapterPosition(position)?.itemView?.let {
                    it.requestFocus()
                    pendingShelf = null
                }
            }
        }
        if (shelf.findViewHolderForAdapterPosition(target) != null) apply()
        else {
            shelf.stopScroll()
            shelf.doOnNextLayout { apply() }
            shelf.scrollToPosition(target)
            shelf.requestLayout()
        }
        return true
    }

    private fun pageRemoteKey(event: KeyEvent): Boolean {
        if (menuOpen || current == Screen.Home || current is Screen.Detail) return false
        val direction =
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> View.FOCUS_LEFT
                KeyEvent.KEYCODE_DPAD_RIGHT -> View.FOCUS_RIGHT
                KeyEvent.KEYCODE_DPAD_UP -> View.FOCUS_UP
                KeyEvent.KEYCODE_DPAD_DOWN -> View.FOCUS_DOWN
                else -> return false
            }
        val focused = list.findFocus() ?: return false
        if (focused is EditText) return false
        val outer = list.findContainingItemView(focused) ?: return false
        val position =
            remotePendingKey?.let { key ->
                adapter.rows.indexOfFirst { it.key == key }.takeIf { it >= 0 }
            } ?: list.getChildAdapterPosition(outer)
        if (position !in adapter.rows.indices) return true
        val horizontal = direction == View.FOCUS_LEFT || direction == View.FOCUS_RIGHT
        if (event.action != KeyEvent.ACTION_DOWN) return true
        if (horizontal && outer is RecyclerView) {
            adapter.rememberShelf(outer)
            val child = outer.findContainingItemView(focused)
            val index = child?.let { outer.getChildAdapterPosition(it) } ?: return true
            val step = if (direction == View.FOCUS_RIGHT) 1 else -1
            val target = index + step
            if (target < 0) {
                if (event.repeatCount == 0) openMenu()
                return true
            }
            if (target < (outer.adapter?.itemCount ?: 0)) {
                outer.findViewHolderForAdapterPosition(target)?.itemView?.requestFocus()
                    ?: run {
                        outer.scrollToPosition(target)
                        outer.post {
                            if (current == model.state.value.screen && outer.hasFocus())
                                outer
                                    .findViewHolderForAdapterPosition(target)
                                    ?.itemView
                                    ?.requestFocus()
                        }
                    }
            }
            return true
        }
        if (horizontal && outer is android.widget.HorizontalScrollView) {
            val choices = outer.getFocusables(View.FOCUS_FORWARD)
            val index = choices.indexOf(focused)
            val step = if (direction == View.FOCUS_RIGHT) 1 else -1
            choices.getOrNull(index + step)?.requestFocus()
                ?: run { if (step < 0 && event.repeatCount == 0) openMenu() }
            return true
        }
        val focusable =
            adapter.rows.map { row ->
                when (row.kind) {
                    Kind.TEXT,
                    Kind.PROFILE -> false
                    Kind.HEADING -> row.click != null
                    else -> true
                }
            }
        val target =
            remoteGridTarget(
                adapter.rows.indices.map { grid.spanSizeLookup.getSpanSize(it) },
                focusable,
                grid.spanCount,
                position,
                direction,
            )
        if (target == null) {
            if (direction == View.FOCUS_LEFT && event.repeatCount == 0) openMenu()
            if (direction == View.FOCUS_DOWN) {
                when {
                    current is Screen.Catalog -> model.loadMoreCatalog()
                    current is Screen.Section -> model.loadMoreSection()
                    isAccountList(current) -> loadAccountReleases(current)
                    current == Screen.AccountHistory -> loadAccountHistory()
                }
            }
            return true
        }
        val screen = current
        val key = adapter.rows[target].key
        val token = ++remoteFocusToken
        remotePendingKey = key
        fun apply() {
            if (token == remoteFocusToken && current == screen && !menuOpen) {
                val index = adapter.rows.indexOfFirst { it.key == key }
                val targetView = list.findViewHolderForAdapterPosition(index)?.itemView
                if (targetView != null) {
                    focusItem(targetView)
                    remotePendingKey = null
                }
            }
        }
        val item = list.findViewHolderForAdapterPosition(target)?.itemView
        if (item != null) {
            apply()
        } else {
            list.stopScroll()
            list.doOnNextLayout { apply() }
            grid.scrollToPositionWithOffset(target, dp(16))
            list.requestLayout()
        }
        return true
    }

    @android.annotation.SuppressLint(
        "RestrictedApi"
    ) // Required for remote key dispatch through ComponentActivity.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (::startup.isInitialized && startup.visibility == View.VISIBLE)
            return super.dispatchKeyEvent(event)
        if (list.findFocus() !is EditText && remoteRepeat.discard(event)) return true
        if (
            event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode in
                    listOf(
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN,
                        KeyEvent.KEYCODE_BACK,
                    )
        ) {
            shelfMoveToken++
            pendingShelf = null
        }
        if (event.keyCode == KeyEvent.KEYCODE_MENU && current != Screen.Welcome) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                remoteClick.cancel()
                if (menuOpen) closeMenu() else openMenu()
            }
            return true
        }

        if (
            menuOpen &&
                event.keyCode in
                    listOf(
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN,
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_DPAD_RIGHT,
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                    )
        ) {
            val focused = menu.findFocus()
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        val choices = menu.getFocusables(View.FOCUS_FORWARD)
                        val index = choices.indexOf(focused)
                        choices
                            .getOrNull(
                                index + if (event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) 1 else -1
                            )
                            ?.requestFocus()
                    }
                KeyEvent.KEYCODE_DPAD_RIGHT -> if (event.action == KeyEvent.ACTION_DOWN) closeMenu()
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER ->
                    if (event.action == KeyEvent.ACTION_UP) focused?.performClick()
            }
            return true
        }
        if (remoteClick.handle(event, list.findFocus())) return true
        if (shelfRemoteKey(event)) return true
        if (pageRemoteKey(event)) return true
        if (detailKey(event)) return true
        if (current == Screen.Home && !menuOpen) {
            val focus = list.findFocus()
            val outer = focus?.let { list.findContainingItemView(it) }
            if (
                event.keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                    event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
            ) {
                if (focus != null) {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        val now = android.os.SystemClock.uptimeMillis()
                        if (event.repeatCount == 0 || now - lastHomeMove >= 140) {
                            lastHomeMove = now
                            moveHomeFocus(if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP) -1 else 1)
                        }
                    }
                    return true
                }
            }
            if (
                outer is RecyclerView &&
                    event.keyCode in listOf(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT)
            ) {
                val child = focus?.let { outer.findContainingItemView(it) }
                val index = child?.let { outer.getChildAdapterPosition(it) } ?: -1
                if (
                    index >= 0 &&
                        event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT &&
                        index == (outer.adapter?.itemCount ?: 0) - 1
                )
                    return true
                if (index == 0 && event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) openMenu()
                    return true
                }
            }
        }
        if (!menuOpen && current != Screen.Welcome && event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            val focus = currentFocus
            val next = focus?.focusSearch(View.FOCUS_LEFT)
            if (focus != null && (next == null || isInMenu(next))) {
                if (event.action == KeyEvent.ACTION_DOWN) openMenu()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun isInMenu(view: View): Boolean {
        var node: android.view.ViewParent? = view.parent
        while (node != null) {
            if (node === menu) return true
            node = node.parent
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list =
            object : RecyclerView(this) {
                    override fun requestChildRectangleOnScreen(
                        child: View,
                        rect: android.graphics.Rect,
                        immediate: Boolean,
                    ): Boolean =
                        if (current == Screen.Home || current is Screen.Detail) false
                        else super.requestChildRectangleOnScreen(child, rect, immediate)
                }
                .apply {
                    setBackgroundColor(0xFF121212.toInt())
                    setPadding(dp(32), dp(8), dp(32), dp(24))
                    clipToPadding = false
                    itemAnimator = null
                }
        grid =
            object : GridLayoutManager(this, 15) {
                override fun onRequestChildFocus(
                    parent: RecyclerView,
                    state: RecyclerView.State,
                    child: View,
                    focused: View?,
                ): Boolean =
                    current == Screen.Home ||
                        current is Screen.Detail ||
                        super.onRequestChildFocus(parent, state, child, focused)
            }
        list.layoutManager = grid
        adapter =
            PageAdapter(
                ::selectTab,
                ::play,
                ::favorite,
                {
                    rememberFocus()
                    model.open(Screen.Detail(it.id))
                },
                { id -> prefs.getLong("position:$id", 0) to prefs.getLong("duration:$id", 0) },
                { id -> prefs.getString("last:$id", null) },
                ::returnToTabs,
                { index ->
                    enterDay = true
                    val previous = model.state.value
                    model.homeDay(index)
                    if (previous == model.state.value) render(model.state.value)
                },
                ::quickActions,
                enterTab = ::enterSelectedTab,
            )
        grid.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) =
                    when (adapter.rows.getOrNull(position)?.kind) {
                        Kind.ACCOUNT_TILE -> 10
                        Kind.POSTER ->
                            if (
                                isAccountList(current) ||
                                    current is Screen.Catalog ||
                                    current is Screen.Section ||
                                    current is Screen.Library ||
                                    current is Screen.Franchise
                            )
                                5
                            else 3
                        Kind.GENRE -> if (current is Screen.Section) 5 else 3
                        Kind.FRANCHISE ->
                            if ((current as? Screen.Section)?.kind == HomeSection.FRANCHISES) 10
                            else 5
                        Kind.EPISODE,
                        Kind.FEATURE -> 5
                        else -> grid.spanCount
                    }
            }
        list.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
                    val state = model.state.value
                    if (
                        isAccountList(current) &&
                            dy > 0 &&
                            grid.findLastVisibleItemPosition() >= adapter.itemCount - 6 &&
                            !accountReleases.loading &&
                            accountReleases.error == null
                    )
                        loadAccountReleases(current)
                    if (
                        current == Screen.AccountHistory &&
                            dy > 0 &&
                            grid.findLastVisibleItemPosition() >= adapter.itemCount - 6 &&
                            accountHistoryError == null
                    )
                        loadAccountHistory()
                    if (
                        state.moreError == null &&
                            dy > 0 &&
                            grid.findLastVisibleItemPosition() >= adapter.itemCount - 9
                    ) {
                        if (state.screen is Screen.Catalog) model.loadMoreCatalog()
                        else if (state.screen is Screen.Section) model.loadMoreSection()
                    }
                }
            }
        )
        list.viewTreeObserver.addOnGlobalFocusChangeListener { _, focus ->
            if (focus != null)
                list.findContainingItemView(focus)?.let { item ->
                    val key = item.tag as? String
                    if (key != null && item is ViewGroup && item !is RecyclerView) {
                        val index = item.getFocusables(View.FOCUS_FORWARD).indexOf(focus)
                        if (index >= 0) childFocusMemory[screenKey(current) to key] = index
                    }
                }
            if (
                current is Screen.Detail &&
                    focus != null &&
                    list.findContainingItemView(focus) != null
            )
                rememberDetailFocus()
            if (
                (current is Screen.Catalog || current is Screen.Section) &&
                    !menuOpen &&
                    focus != null
            ) {
                val item = list.findContainingItemView(focus)
                val position = item?.let { list.getChildAdapterPosition(it) } ?: -1
                val state = model.state.value
                if (
                    item is RecyclerView &&
                        adapter.rows.getOrNull(position)?.key?.startsWith("period-shelf:") ==
                            true &&
                        state.more &&
                        state.moreError == null
                ) {
                    val nestedItem = item.findContainingItemView(focus)
                    val nestedPosition = nestedItem?.let { item.getChildAdapterPosition(it) } ?: -1
                    if (nestedPosition >= ((item.adapter?.itemCount ?: 0) - 5).coerceAtLeast(0))
                        list.post {
                            if (model.state.value.screen == state.screen) model.loadMoreSection()
                        }
                }
                if (
                    position >= 0 &&
                        adapter.rows.getOrNull(position)?.kind in
                            listOf(Kind.POSTER, Kind.GENRE, Kind.FRANCHISE, Kind.FEATURE) &&
                        position >= adapter.itemCount - 9 &&
                        state.more &&
                        state.moreError == null
                ) {
                    list.post {
                        if (
                            model.state.value.screen == state.screen &&
                                model.state.value.moreError == null
                        ) {
                            if (state.screen is Screen.Section) model.loadMoreSection()
                            else model.loadMoreCatalog()
                        }
                    }
                }
            }
        }
        list.adapter = adapter
        shell = LinearLayout(this).apply { setBackgroundColor(0xFF121212.toInt()) }
        menu =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(4), dp(12), dp(4), 0)
                setBackgroundColor(0xFF212121.toInt())
            }
        shell.addView(menu, LinearLayout.LayoutParams(dp(60), -1))
        shell.addView(list, LinearLayout.LayoutParams(0, -1, 1f))
        buildMenu()
        root =
            android.widget.FrameLayout(this).apply {
                addView(shell, android.widget.FrameLayout.LayoutParams(-1, -1))
            }
        startup =
            StartupScreen(
                this,
                start = {
                    getSharedPreferences("viewing", MODE_PRIVATE)
                        .edit()
                        .putBoolean("welcomed", true)
                        .apply()
                    model.open(Screen.Home, false)
                },
                retry = model::retry,
                qr = { login("qr") },
                password = { login("password") },
            )
        root.addView(startup, android.widget.FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    remoteClick.cancel()
                    remotePendingKey = null
                    remoteFocusToken++
                    shelfMoveToken++
                    pendingShelf = null
                    if (catalogSearchEditing) {
                        finishCatalogSearch()
                        return
                    }
                    if (menuOpen) {
                        closeMenu()
                        return
                    }
                    rememberFocus()
                    if (current == Screen.Home) {
                        if (list.computeVerticalScrollOffset() > 0) {
                            val index = adapter.rows.indexOfFirst { it.kind == Kind.SHELF }
                            if (index >= 0) {
                                grid.scrollToPositionWithOffset(0, 0)
                                list.doOnNextLayout {
                                    focusItem(
                                        list.findViewHolderForAdapterPosition(index)?.itemView
                                    )
                                }
                                list.requestLayout()
                            }
                        } else finish()
                    } else if (!model.back()) finish()
                }
            },
        )
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { model.state.collect { render(it) } }
        }
        if (
            model.state.value.screen == Screen.Welcome &&
                getSharedPreferences("viewing", MODE_PRIVATE).getBoolean("welcomed", false)
        )
            model.open(Screen.Home, false)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (
            hasFocus &&
                ::adapter.isInitialized &&
                current is Screen.Detail &&
                !menuOpen &&
                detailAddress() == null
        ) {
            val screen = current as Screen.Detail
            val bookmark = detailBookmarks[screen.id to screen.tab]
            val index =
                adapter.rows.indexOfFirst { it.key == bookmark?.row }.takeIf { it >= 0 }
                    ?: adapter.rows.indexOfFirst { it.kind == Kind.HERO }
            if (index >= 0) focusDetail(index, bookmark?.child)
        }
    }

    override fun onStop() {
        remoteClick.cancel()
        remoteFocusToken++
        shelfMoveToken++
        pendingShelf = null
        remotePendingKey = null
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (resumedOnce && ::adapter.isInitialized) {
            rememberFocus()
            render(model.state.value)
        }
        resumedOnce = true
        hydrateHistory()
        if (account.signedIn) refreshAccount(force = true)
    }

    private fun hydrateHistory() {
        if (historyHydrating || System.currentTimeMillis() - lastHistoryHydration < 60000) return
        historyHydrating = true
        lastHistoryHydration = System.currentTimeMillis()
        lifecycleScope.launch {
            try {
                val known = history.latestWatched().associateBy { it.releaseId }
                val legacy =
                    prefs.all.keys
                        .filter { it.startsWith("last:") }
                        .mapNotNull { it.substringAfter(":").toIntOrNull() }
                        .filter { it !in known }
                val candidates =
                    (history.hiddenReleaseIds() +
                            known.values.filter { it.completed }.map { it.releaseId } +
                            legacy)
                        .distinct()
                        .take(16)
                for (id in candidates) {
                    try {
                        val item = model.api.release(id, refresh = true)
                        val saved = known[id]
                        val uuid = saved?.episodeId ?: prefs.getString("last:$id", null) ?: continue
                        val episode = item.episodes.firstOrNull { it.id == uuid } ?: continue
                        if (history.latestWatched().firstOrNull { it.releaseId == id } != saved)
                            continue
                        history.save(
                            item,
                            episode,
                            saved?.position ?: prefs.getLong("position:$uuid", 0),
                            saved?.duration
                                ?: prefs.getLong("duration:$uuid", episode.duration * 1000),
                            saved?.updatedAt ?: 0,
                        )
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        /* Existing local cards remain usable. */
                    }
                }
                if (current == Screen.Home && candidates.isNotEmpty()) render(model.state.value)
            } finally {
                historyHydrating = false
            }
        }
    }

    private fun playContinue(item: ContinueEpisode) {
        rememberFocus()
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("release", item.releaseId)
                .putExtra("episode", item.episodeId)
        )
    }

    private fun rememberFocus() {
        rememberDetailFocus()
        list
            .findFocus()
            ?.let { v -> list.findContainingItemView(v)?.tag as? String }
            ?.let { focusMemory[screenKey(current)] = it }
        val first = grid.findFirstVisibleItemPosition()
        adapter.rows.getOrNull(first)?.key?.let {
            scrollMemory[screenKey(current)] =
                it to (grid.findViewByPosition(first)?.top ?: list.paddingTop)
        }
    }

    private fun navigate(index: Int) {
        rememberFocus()
        when (index) {
            0 -> model.open(Screen.Home)
            1 -> model.open(Screen.Catalog())
            2 -> search()
            9 -> openSection(HomeSection.SCHEDULE)
            3 -> library()
            4 -> openAccountList(Screen.Favorites())
            5 -> openAccountList(Screen.Collections())
            6 -> openAccountHistory()
            7 -> model.open(Screen.Settings)
            8 ->
                lifecycleScope.launch {
                    try {
                        val r = model.api.randomRelease()
                        model.open(Screen.Detail(r.id))
                    } catch (_: Exception) {
                        Toast.makeText(
                                this@MainActivity,
                                "Не удалось выбрать релиз",
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                    }
                }
        }
    }

    private fun search() {
        val screen = (model.state.value.screen as? Screen.Catalog) ?: Screen.Catalog()
        if (model.state.value.screen != screen) model.open(screen)
        catalogSearch(screen)
    }

    private var enterCatalogResults = false
    private var catalogReferences: Map<String, List<CatalogChoice>> = emptyMap()

    private fun applyCatalog(screen: Screen.Catalog) {
        rememberFocus()
        val next = screen.copy(page = 1)
        if (next == model.state.value.screen && !model.state.value.loading) {
            enterCatalogResults = false
            val index = adapter.rows.indexOfFirst { it.kind == Kind.POSTER }
            if (index >= 0) {
                list.doOnNextLayout {
                    focusItem(list.findViewHolderForAdapterPosition(index)?.itemView)
                }
                grid.scrollToPositionWithOffset(index, list.dp(96))
            }
            return
        }
        enterCatalogResults = true
        model.open(next, false)
    }

    private var scheduleFavorites = false
    private var scheduleCollections = false
    private var scheduleFilterUser: Int? = null

    private fun toggleScheduleSource(favorites: Boolean) {
        val user = account.session.userId
        fun apply() {
            if (account.session.userId == user) {
                if (favorites) scheduleFavorites = !scheduleFavorites
                else scheduleCollections = !scheduleCollections
                rememberFocus()
                render(model.state.value)
            }
        }
        if (account.lists == null) accountAction(done = { apply() }) { account.refresh() }
        else apply()
    }

    private var scheduleDay = java.time.LocalDate.now().dayOfWeek.value
    private val sectionQueries = mutableMapOf<HomeSection, String>()
    private var catalogSearchEditing = false
    private var catalogSearchDraft = ""
    private var catalogSearchJob: kotlinx.coroutines.Job? = null

    private fun catalogSearch(screen: Screen.Catalog) {
        catalogSearchEditing = true
        catalogSearchDraft = screen.query
        render(model.state.value)
    }

    private fun liveCatalogSearch(query: String) {
        catalogSearchDraft = query
        catalogSearchJob?.cancel()
        catalogSearchJob =
            lifecycleScope.launch {
                kotlinx.coroutines.delay(350)
                val active = model.state.value.screen
                if (active is Screen.Section) {
                    sectionQueries[active.kind] = query
                    render(model.state.value)
                    return@launch
                }
                if (isAccountList(active)) {
                    val next = withAccountQuery(active, query.trim())
                    if (next != active) model.open(next, false)
                    return@launch
                }
                val screen = active as? Screen.Catalog ?: return@launch
                val next = screen.copy(query = query.trim(), page = 1)
                if (next != screen) model.open(next, false)
            }
    }

    private fun finishCatalogSearch() {
        catalogSearchJob?.cancel()
        catalogSearchEditing = false
        (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
            .hideSoftInputFromWindow(list.windowToken, 0)
        val active = model.state.value.screen
        if (active is Screen.Section) {
            sectionQueries[active.kind] = catalogSearchDraft
            enterCatalogResults = true
            render(model.state.value)
            return
        }
        if (isAccountList(active)) {
            enterCatalogResults = true
            val next = withAccountQuery(active, catalogSearchDraft.trim())
            if (next == active) render(model.state.value) else model.open(next, false)
            return
        }
        val screen = active as? Screen.Catalog ?: return
        enterCatalogResults = true
        val next = screen.copy(query = catalogSearchDraft.trim(), page = 1)
        if (next == screen) render(model.state.value) else model.open(next, false)
    }

    private fun withCatalogReferences(
        screen: Screen.Catalog,
        ready: (Map<String, List<CatalogChoice>>) -> Unit,
    ) {
        if (catalogReferences.isNotEmpty()) {
            ready(catalogReferences)
            return
        }
        val panel = TvSidePanel(this, "Каталог")
        panel.message("Загрузка параметров…")
        var task: kotlinx.coroutines.Job? = null
        panel.addAction("Отмена") {
            task?.cancel()
            panel.dismiss()
        }
        panel.setOnCancelListener { task?.cancel() }
        panel.show()
        task =
            lifecycleScope.launch {
                try {
                    val data = model.api.catalogReferences()
                    catalogReferences = data
                    panel.dismiss()
                    if (model.state.value.screen == screen) ready(data)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    panel.dismiss()
                    throw e
                } catch (_: Exception) {
                    if (model.state.value.screen == screen) {
                        panel.message("Не удалось загрузить параметры")
                        panel.footer.removeAllViews()
                        panel.addAction("Повторить", true) {
                            panel.dismiss()
                            withCatalogReferences(screen, ready)
                        }
                        panel.addAction("Закрыть") { panel.dismiss() }
                        panel.footer.getChildAt(0)?.requestFocus()
                    } else panel.dismiss()
                }
            }
    }

    private fun catalogFilters(screen: Screen.Catalog) =
        withCatalogReferences(screen) { refs ->
            CatalogFilterPanel(this, screen, refs) { filters ->
                    applyCatalog(screen.copy(filters = filters, genre = null, title = null))
                }
                .show()
        }

    private fun catalogSorting(screen: Screen.Catalog) =
        withCatalogReferences(screen) { refs ->
            val panel = TvSidePanel(this, "Сортировка")
            refs["sorting"].orEmpty().forEach { choice ->
                panel.addChoice(choice.title, choice.value == screen.filters.sorting) {
                    panel.dismiss()
                    applyCatalog(screen.copy(filters = screen.filters.copy(sorting = choice.value)))
                }
            }
            panel.addAction("Отмена") { panel.dismiss() }
            panel.show()
        }

    private fun catalogSummary(screen: Screen.Catalog): String {
        val f = screen.filters
        return listOf(
                screen.query.takeIf { it.isNotBlank() }?.let { "Поиск: $it" }.orEmpty(),
                if (screen.genre != null) screen.title ?: "Выбранный жанр"
                else if (f.genres.isNotEmpty()) "Жанры: ${f.genres.size}" else "",
                if (f.types.isNotEmpty()) "Формат: ${f.types.size}" else "",
                if (f.fromYear.isNotBlank() || f.toYear.isNotBlank())
                    "${f.fromYear.ifBlank{"…"}}–${f.toYear.ifBlank{"…"}}"
                else "",
                if (f.seasons.isNotEmpty()) "Сезоны: ${f.seasons.size}" else "",
                if (f.publish.isNotEmpty()) "Статус выхода" else "",
                if (f.production.isNotEmpty()) "Статус озвучки" else "",
                if (f.ages.isNotEmpty()) "Возраст" else "",
            )
            .filter(String::isNotBlank)
            .joinToString(" · ")
    }

    private fun login(method: String? = null) {
        val fromWelcome = current == Screen.Welcome
        val previousUser = account.session.userId
        val panel =
            AccountPanel(this, lifecycleScope, account) {
                accountJob?.cancel()
                accountJob = null
                profileSyncing = false
                accountHistoryJob?.cancel()
                accountHistoryLoadJob?.cancel()
                accountHistoryItems = emptyList()
                accountSessions = emptyList()
                accountHistoryPage = 0
                profileError = null
                lastHistoryHydration = 0L
                accountLoadedAt = 0L
                Toast.makeText(this, "Вход выполнен", Toast.LENGTH_SHORT).show()
                if (previousUser != null && previousUser != account.session.userId)
                    model.resetNavigation(Screen.Profile)
                if (fromWelcome) {
                    getSharedPreferences("viewing", MODE_PRIVATE)
                        .edit()
                        .putBoolean("welcomed", true)
                        .apply()
                    model.open(Screen.Home, false)
                }
                rememberFocus()
                render(model.state.value)
                refreshAccount(force = true, after = { offerLocalHistory(automatic = true) })
            }
        when (method) {
            "qr" -> panel.startQr()
            "password" -> panel.startPassword()
            else -> panel.start()
        }
    }

    private fun refreshAccount(force: Boolean = false, after: (() -> Unit)? = null) {
        if (!account.signedIn || accountJob?.isActive == true) return
        if (!force && System.currentTimeMillis() - accountLoadedAt < 60000) return
        profileSyncing = true
        profileError = null
        if (current == Screen.Profile || current is Screen.AccountSettings)
            render(model.state.value)
        accountJob =
            lifecycleScope.launch {
                try {
                    account.refresh()
                    accountLoadedAt = System.currentTimeMillis()
                    account.loadProgress()
                    rememberFocus()
                    render(model.state.value)
                    startAccountHistorySync()
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    profileError = e.message ?: "Проверьте соединение"
                } finally {
                    profileSyncing = false
                    if (account.signedIn) after?.invoke()
                    if (current == Screen.Profile || current is Screen.AccountSettings)
                        render(model.state.value)
                }
            }
    }

    private fun startAccountHistorySync() {
        if (accountHistoryJob?.isActive == true) return
        accountHistoryJob =
            lifecycleScope.launch {
                hydrateAccountHistory()
                if (account.signedIn) {
                    rememberFocus()
                    render(model.state.value)
                }
            }
    }

    private suspend fun hydrateAccountHistory() {
        val user = account.session.userId ?: return
        try {
            val recent = account.recentHistory().distinctBy { it.releaseId }.take(12)
            for (record in recent) {
                if (account.session.userId != user) return
                val local = history.latestWatched().firstOrNull { it.releaseId == record.releaseId }
                if (local != null && local.updatedAt > record.updatedAt) continue
                try {
                    val release = model.api.release(record.releaseId)
                    if (account.session.userId != user) return
                    account.applyProgress(release)
                    val episode =
                        release.episodes.firstOrNull { it.id == record.episodeId } ?: continue
                    history.save(
                        release,
                        episode,
                        prefs.getLong("position:${episode.id}", 0),
                        prefs.getLong("duration:${episode.id}", episode.duration * 1000),
                        record.updatedAt,
                    )
                    prefs.edit().putString("last:${release.id}", episode.id).apply()
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    /* Keep already available local cards. */
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            /* Progress sync itself can succeed while history metadata is unavailable. */
        }
    }

    private fun accountAction(done: (() -> Unit)? = null, block: suspend () -> Unit) {
        if (accountJob?.isActive == true) {
            Toast.makeText(this, "Подождите, идёт синхронизация", Toast.LENGTH_SHORT).show()
            return
        }
        accountJob =
            lifecycleScope.launch {
                try {
                    block()
                    if (!isAccountList(current)) accountReleaseScreen = null
                    rememberFocus()
                    render(model.state.value)
                    done?.invoke()
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val panel = TvSidePanel(this@MainActivity, "Не удалось сохранить")
                    panel.message(e.message ?: "Проверьте подключение")
                    panel.addAction("Повторить", true) {
                        panel.dismiss()
                        accountAction(done, block)
                    }
                    if (e is AccountHttpException && e.status in listOf(401, 403))
                        panel.addAction("Войти заново") {
                            panel.dismiss()
                            login()
                        }
                    panel.addAction("Закрыть") { panel.dismiss() }
                    panel.show()
                    if (!account.signedIn) render(model.state.value)
                }
            }
    }

    private var profileError: String? = null
    private var profileSyncing = false
    private var accountHistoryItems: List<AccountViewedEpisode> = emptyList()
    private var accountHistoryPage = 0
    private var accountHistoryMore = false
    private var accountHistoryLoading = false
    private var accountHistoryError: String? = null
    private var accountHistoryLoadJob: kotlinx.coroutines.Job? = null

    private fun library() {
        rememberFocus()
        model.open(Screen.Profile)
        if (account.signedIn) refreshAccount(force = true)
    }

    private var accountReleases: AccountReleaseState = AccountReleaseState()
    private var accountReleaseScreen: Screen? = null
    private var accountReleaseUser: Int? = null
    private var accountReleaseJob: kotlinx.coroutines.Job? = null

    private fun openAccountList(screen: Screen) {
        rememberFocus()
        model.open(screen)
    }

    private fun loadAccountReleases(screen: Screen, reset: Boolean = false) {
        val user = account.session.userId
        if (reset || screen != accountReleaseScreen || user != accountReleaseUser) {
            accountReleaseJob?.cancel()
            accountReleaseScreen = screen
            accountReleaseUser = user
            accountReleases = AccountReleaseState()
        } else if (accountReleases.loading || (accountReleases.page > 0 && !accountReleases.more))
            return
        val page = accountReleases.page + 1
        accountReleases = accountReleases.copy(loading = true, error = null)
        accountReleaseJob =
            lifecycleScope.launch {
                try {
                    val result = account.releases(screen, page)
                    if (accountReleaseScreen == screen && account.session.userId == user) {
                        accountReleases =
                            AccountReleaseState(
                                (accountReleases.items + result.releases).distinctBy { it.id },
                                page,
                                result.more,
                                result.total,
                            )
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (accountReleaseScreen == screen && account.session.userId == user)
                        accountReleases =
                            accountReleases.copy(
                                loading = false,
                                error = e.message ?: "Не удалось загрузить список",
                            )
                } finally {
                    if (current == screen) render(model.state.value)
                }
            }
    }

    private fun accountFilters(screen: Screen, sort: Boolean = false) {
        accountAction {
            val refs = account.references(screen is Screen.Favorites)
            if (current == screen) {
                if (sort) {
                    TvSidePanel(this@MainActivity, "Сортировка избранного").apply {
                        refs["sorting"].orEmpty().forEach { choice ->
                            addChoice(
                                choice.title,
                                choice.value == accountListFilter(screen).sorting,
                            ) {
                                dismiss()
                                model.open(
                                    withAccountFilter(
                                        screen,
                                        accountListFilter(screen).copy(sorting = choice.value),
                                    ),
                                    false,
                                )
                            }
                        }
                        addAction("Отмена") { dismiss() }
                        show()
                    }
                } else
                    AccountFilterPanel(this@MainActivity, accountListFilter(screen), refs) {
                            model.open(withAccountFilter(screen, it), false)
                        }
                        .show()
            }
        }
    }

    private var accountSessions: List<AccountDeviceSession> = emptyList()
    private var accountSessionsLoading = false
    private var accountSessionsError: String? = null

    private fun loadAccountSessions() {
        if (accountSessionsLoading || !account.signedIn) return
        accountSessionsLoading = true
        accountSessionsError = null
        if (current == Screen.AccountSessions) render(model.state.value)
        lifecycleScope.launch {
            val user = account.session.userId
            try {
                val sessions = account.sessions()
                if (account.session.userId == user) accountSessions = sessions
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                accountSessionsError = e.message ?: "Не удалось загрузить сеансы"
            } finally {
                accountSessionsLoading = false
                if (current == Screen.AccountSessions) render(model.state.value)
            }
        }
    }

    private fun editAccountIdentity(nickname: Boolean) {
        val panel = TvSidePanel(this, if (nickname) "Имя в профиле" else "Логин")
        val input =
            EditText(this).apply {
                setSingleLine()
                setText(
                    if (nickname) account.session.profile()?.name
                    else account.session.profile()?.login
                )
                textSize = 18f
                setTextColor(WHITE)
                setPadding(dp(12), dp(12), dp(12), dp(12))
                webControlBackground()
                inputType = android.text.InputType.TYPE_CLASS_TEXT
                imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_EXTRACT_UI
            }
        panel.body.addView(input, LinearLayout.LayoutParams(-1, -2))
        val save =
            panel.addAction("Сохранить", true) {
                val value = input.text.toString().trim()
                if (value.isBlank()) {
                    input.error = "Заполните поле"
                } else
                    accountAction(done = { panel.dismiss() }) {
                        account.updateIdentity(nickname, value)
                    }
            }
        input.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                save.requestFocus()
                true
            } else false
        }
        panel.addAction("Отмена") { panel.dismiss() }
        panel.show()
        input.requestFocus()
        input.setSelection(input.text.length)
    }

    private fun accountSettingsOnPhone() {
        val url = "https://www.aniliberty.top/app/settings/account"
        val panel = TvSidePanel(this, "Настройки аккаунта")
        panel.message("Отсканируйте QR-код для изменения данных аккаунта.")
        val matrix =
            com.google.zxing.qrcode
                .QRCodeWriter()
                .encode(url, com.google.zxing.BarcodeFormat.QR_CODE, 256, 256)
        val image =
            android.graphics.Bitmap.createBitmap(256, 256, android.graphics.Bitmap.Config.ARGB_8888)
        image.setPixels(
            IntArray(256 * 256) { i ->
                if (matrix[i % 256, i / 256]) android.graphics.Color.BLACK
                else android.graphics.Color.WHITE
            },
            0,
            256,
            0,
            0,
            256,
            256,
        )
        panel.body.addView(
            android.widget.ImageView(this).apply {
                setImageBitmap(image)
                contentDescription = "QR-код настроек аккаунта"
            },
            LinearLayout.LayoutParams(dp(180), dp(180)).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            },
        )
        panel.addAction("Открыть на ТВ") { openLink(url) }
        panel.addAction("Закрыть") { panel.dismiss() }
        panel.show()
    }

    private fun confirmLogout() {
        TvSidePanel(this, "Выйти из аккаунта?").apply {
            message(
                "Данные аккаунта останутся на сайте. Приложение переключится на гостевую историю этого ТВ."
            )
            addAction("Выйти") {
                dismiss()
                accountHistoryLoadJob?.cancel()
                accountHistoryJob?.cancel()
                model.resetNavigation(Screen.Profile)
                accountAction(
                    done = {
                        lastHistoryHydration = 0
                        accountHistoryItems = emptyList()
                        accountSessions = emptyList()
                        accountHistoryPage = 0
                        model.open(Screen.Profile, false)
                    }
                ) {
                    if (!account.logout())
                        Toast.makeText(
                                this@MainActivity,
                                "Вышли на этом ТВ. Сервис не подтвердил завершение сеанса.",
                                Toast.LENGTH_LONG,
                            )
                            .show()
                }
            }
            addAction("Отмена") { dismiss() }
            show()
        }
    }

    private fun openAccountHistory() {
        rememberFocus()
        model.open(Screen.AccountHistory)
        loadAccountHistory(reset = true)
    }

    private fun loadAccountHistory(reset: Boolean = false) {
        if (accountHistoryLoading || (!reset && !accountHistoryMore)) return
        val user = account.session.userId
        if (reset) {
            accountHistoryItems = emptyList()
            accountHistoryPage = 0
        }
        accountHistoryLoading = true
        accountHistoryError = null
        if (current == Screen.AccountHistory) render(model.state.value)
        accountHistoryLoadJob =
            lifecycleScope.launch {
                try {
                    val next = account.history(accountHistoryPage + 1)
                    if (account.session.userId != user) return@launch
                    accountHistoryItems =
                        (accountHistoryItems + next.items).distinctBy { it.episode.id }
                    accountHistoryPage++
                    accountHistoryMore = next.more
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    accountHistoryError = e.message ?: "Не удалось загрузить историю"
                } finally {
                    accountHistoryLoading = false
                    if (current == Screen.AccountHistory) render(model.state.value)
                }
            }
    }

    private fun offerLocalHistory(automatic: Boolean = false) {
        val guestLists = account.local.lists()
        val episodes = localTimecodes(getSharedPreferences("viewing", MODE_PRIVATE))
        if (
            episodes.isEmpty() && guestLists.favorites.isEmpty() && guestLists.collections.isEmpty()
        ) {
            if (!automatic)
                Toast.makeText(
                        this,
                        "На этом ТВ нет локальных данных для переноса",
                        Toast.LENGTH_SHORT,
                    )
                    .show()
            return
        }
        val user = account.session.userId ?: return
        list.post {
            if (account.session.userId != user) return@post
            val panel = TvSidePanel(this, "Перенести данные с ТВ в аккаунт?")
            panel.message(
                "Избранное: ${guestLists.favorites.size}. Коллекции: ${guestLists.collections.size}. Прогресс серий: ${episodes.size}.\nУже сохранённые коллекции и прогресс аккаунта не изменятся. Локальные данные останутся на ТВ."
            )
            panel.addAction("Перенести", true) {
                if (accountJob?.isActive == true) return@addAction
                panel.dismiss()
                accountAction {
                    check(account.session.userId == user) { "Аккаунт изменился" }
                    account.importLocalHistory()
                    accountReleaseScreen = null
                    startAccountHistorySync()
                    Toast.makeText(this, "Данные перенесены", Toast.LENGTH_LONG).show()
                }
            }
            panel.addAction("Не переносить") { panel.dismiss() }
            panel.show()
        }
    }

    private fun collections(release: Release) {
        if (!account.signedIn) account.local.remember(release)
        if (account.lists == null) {
            accountAction(done = { collections(release) }) { account.refresh() }
            return
        }
        val current = account.lists!!.collections[release.id]
        val panel = TvSidePanel(this, "Коллекция")
        panel.message(release.title)
        CollectionType.entries.forEach { type ->
            panel.addChoice(type.title, current == type, type.icon, iconTint = type.color) {
                panel.dismiss()
                accountAction {
                    account.collection(release.id, type)
                    accountReleaseScreen = null
                }
            }
        }
        if (current != null)
            panel.addChoice("Убрать из коллекции", icon = R.drawable.ic_close) {
                panel.dismiss()
                accountAction {
                    account.collection(release.id, null)
                    accountReleaseScreen = null
                }
            }
        panel.addAction("Отмена") { panel.dismiss() }
        panel.show()
    }

    private fun setEpisodeWatched(release: Release, episodes: List<Episode>, watched: Boolean) {
        val edit = prefs.edit()
        episodes.forEach { e ->
            val duration = (e.duration * 1000).coerceAtLeast(1)
            edit
                .putLong("position:${e.id}", if (watched) duration else 0)
                .putLong("duration:${e.id}", duration)
        }
        edit.apply()
        queueAccountTimecodes(
            this,
            episodes.map { e ->
                AccountTimecode(e.id, prefs.getLong("position:${e.id}", 0) / 1000.0, watched)
            },
        )
        episodes.lastOrNull()?.let { e ->
            history.save(
                release,
                e,
                prefs.getLong("position:${e.id}", 0),
                prefs.getLong("duration:${e.id}", 0),
            )
        }
        if (episodes.isNotEmpty()) {
            if (watched) prefs.edit().putString("last:${release.id}", episodes.last().id).apply()
            else prefs.edit().remove("last:${release.id}").apply()
        }
        rememberFocus()
        render(model.state.value)
        if (account.signedIn)
            accountAction {
                account.flush()
                account.loadProgress()
            }
    }

    private fun episodeActions(release: Release, episode: Episode) {
        val completed =
            isEpisodeWatched(
                prefs.getLong("position:${episode.id}", 0),
                prefs.getLong("duration:${episode.id}", episode.duration * 1000),
                settings.watchedPercent,
            )
        val panel = TvSidePanel(this, episode.label)
        if (episode.streams.isNotEmpty())
            panel.addChoice("Смотреть с начала", icon = R.drawable.ic_play) {
                panel.dismiss()
                prefs.edit().putLong("position:${episode.id}", 0).apply()
                play(episode)
            }
        panel.addChoice(
            if (completed) "Отметить непросмотренной" else "Отметить просмотренной",
            icon = R.drawable.ic_episodes,
        ) {
            panel.dismiss()
            setEpisodeWatched(release, listOf(episode), !completed)
        }
        panel.addAction("Закрыть") { panel.dismiss() }
        panel.show()
    }

    private fun releaseWatchActions(release: Release) {
        val panel = TvSidePanel(this, "Отметки просмотра")
        panel.message(
            "Для всех ${release.episodes.size} серий «${release.title}». Коллекция тайтла меняется отдельно."
        )
        panel.addChoice("Все серии просмотрены", icon = R.drawable.ic_episodes) {
            panel.dismiss()
            setEpisodeWatched(release, release.episodes, true)
        }
        panel.addChoice("Все серии не просмотрены", icon = R.drawable.ic_refresh) {
            panel.dismiss()
            setEpisodeWatched(release, release.episodes, false)
        }
        panel.addAction("Отмена") { panel.dismiss() }
        panel.show()
    }

    private fun localLibrary() {
        openAccountList(Screen.Favorites())
    }

    private fun isFavorite(id: Int): Boolean = account.lists?.favorites?.contains(id) == true

    // A successful account mutation can precede a refreshed catalog response.
    // Keep the displayed aggregate in sync across copies of the same release.
    private val favoriteCountOverrides = mutableMapOf<Pair<Int?, Int>, Long>()

    private fun favoriteCountRelease(release: Release): Release {
        if (!account.signedIn) return release
        val count = favoriteCountOverrides[account.session.userId to release.id] ?: return release
        return release.copy(favoriteCount = count)
    }

    private fun withFavoriteState(items: List<Row>): List<Row> =
        items.map { row ->
            row.copy(
                release = row.release?.let(::favoriteCountRelease),
                isFavorite = row.release?.let { isFavorite(it.id) } ?: false,
                collectionType = row.release?.let { account.lists?.collections?.get(it.id) },
                cards =
                    if (row.cards.isNotEmpty()) withFavoriteState(row.cards)
                    else if (row.items.isNotEmpty())
                        row.items.map {
                            Row(
                                "${row.key}:release:${it.id}",
                                Kind.POSTER,
                                release = favoriteCountRelease(it),
                                isFavorite = isFavorite(it.id),
                                collectionType = account.lists?.collections?.get(it.id),
                            )
                        }
                    else emptyList(),
            )
        }

    private fun favorite(release: Release) {
        if (!account.signedIn) account.local.remember(release)
        accountAction {
            if (account.lists == null) account.refresh()
            val added = !isFavorite(release.id)
            account.favorite(release.id, added)
            if (account.signedIn) {
                val key = account.session.userId to release.id
                val previous = favoriteCountOverrides[key] ?: release.favoriteCount
                if (previous != null)
                    favoriteCountOverrides[key] =
                        (previous + (if (added) 1L else -1L)).coerceAtLeast(0L)
            }
            // A release removed from Favorites/Collections must disappear from that list too.
            accountReleaseScreen = null
            Toast.makeText(
                    this,
                    if (added) "Добавлено в избранное" else "Удалено из избранного",
                    Toast.LENGTH_SHORT,
                )
                .show()
        }
    }

    private fun continueActions(item: ContinueEpisode) {
        val panel = TvSidePanel(this, item.releaseTitle)
        panel.message(
            listOf(item.episodeTitle, if (item.nextUp) "Следующая серия" else "").joinToString("\n")
        )
        panel.addChoice(
            if (item.nextUp) "Смотреть следующую серию" else "Продолжить",
            icon = R.drawable.ic_play,
        ) {
            panel.dismiss()
            playContinue(item)
        }
        panel.addChoice("Открыть тайтл", icon = R.drawable.ic_catalog) {
            panel.dismiss()
            rememberFocus()
            model.open(Screen.Detail(item.releaseId))
        }
        panel.addChoice("Убрать из продолжения", icon = R.drawable.ic_close) {
            panel.dismiss()
            history.dismiss(item.releaseId)
            adapter.resetShelf("shelf:continue", true)
            render(model.state.value)
        }
        panel.body.addView(
            android.widget.TextView(this).apply {
                label("Появится снова при открытии тайтла или выходе новой серии.", 13f, MUTED)
                setPadding(0, dp(12), 0, 0)
            }
        )
        panel.addAction("Отмена") { panel.dismiss() }
        panel.show()
    }

    private fun quickActions(item: Release) {
        val resume = prefs.getString("last:${item.id}", null) != null
        val added = isFavorite(item.id)
        val labels =
            arrayOf(
                if (resume) "Продолжить просмотр" else "Смотреть",
                if (account.signedIn) {
                    if (added) "Убрать из избранного" else "В избранное"
                } else if (added) "Убрать с этого ТВ" else "В избранное",
                "Подробнее",
            )
        fun choose(choice: Int) {
            when (choice) {
                0 ->
                    lifecycleScope.launch {
                        try {
                            val release = model.api.release(item.id)
                            val last = prefs.getString("last:${release.id}", null)
                            val episode =
                                release.episodes.firstOrNull { it.id == last }
                                    ?: release.episodes.firstOrNull()
                            if (release.blocked || episode == null || episode.streams.isEmpty())
                                Toast.makeText(
                                        this@MainActivity,
                                        "Просмотр пока недоступен",
                                        Toast.LENGTH_LONG,
                                    )
                                    .show()
                            else {
                                rememberFocus()
                                startActivity(
                                    Intent(this@MainActivity, PlayerActivity::class.java)
                                        .putExtra("release", release.id)
                                        .putExtra("episode", episode.id)
                                )
                            }
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            Toast.makeText(
                                    this@MainActivity,
                                    "Не удалось загрузить тайтл",
                                    Toast.LENGTH_LONG,
                                )
                                .show()
                        }
                    }
                1 -> favorite(item)
                2 -> {
                    rememberFocus()
                    model.open(Screen.Detail(item.id))
                }
            }
        }
        run {
            val panel = TvSidePanel(this, item.title)
            labels.forEachIndexed { index, label ->
                panel.addChoice(label) {
                    panel.dismiss()
                    choose(index)
                }
            }
            panel.addAction("Закрыть") { panel.dismiss() }
            panel.show()
        }
    }

    private fun play(episode: Episode) {
        val release = model.state.value.detail ?: return
        if (release.blocked) {
            Toast.makeText(this, "Просмотр этого тайтла ограничен", Toast.LENGTH_LONG).show()
            return
        }
        if (episode.streams.isEmpty()) {
            Toast.makeText(this, "Серия пока недоступна", Toast.LENGTH_LONG).show()
            return
        }
        rememberFocus()
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("release", release.id)
                .putExtra("episode", episode.id)
        )
    }

    private fun selectTab(index: Int) {
        enterContent = false
        val previous = model.state.value
        model.tab(index)
        if (previous == model.state.value) render(model.state.value)
    }

    private fun enterSelectedTab(index: Int) {
        enterContent = true
        val previous = model.state.value
        model.tab(index)
        if (previous == model.state.value) render(model.state.value)
    }

    private fun returnToTabs() {
        focusDetailTab()
    }

    private fun openLink(url: String?) {
        if (url == null) return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Нет приложения для открытия ссылки", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDetailMore(release: Release) {
        val panel = TvSidePanel(this, "Дополнительно")
        panel.addChoice("Открыть страницу на сайте", icon = R.drawable.ic_arrow_right) {
            panel.dismiss()
            openLink("https://www.aniliberty.top/anime/releases/release/${release.alias}/episodes")
        }
        panel.addChoice("Отметки просмотра серий", icon = R.drawable.ic_episodes) {
            panel.dismiss()
            releaseWatchActions(release)
        }
        panel.addChoice(
            if (account.signedIn) "Аккаунт · ${account.session.name}" else "Войти в аккаунт",
            icon = R.drawable.ic_library,
        ) {
            panel.dismiss()
            library()
        }
        panel.addChoice("Торренты", icon = R.drawable.ic_download) {
            panel.dismiss()
            showTorrents(release.id)
        }
        panel.addAction("Закрыть") { panel.dismiss() }
        panel.show()
    }

    private fun showTorrents(releaseId: Int) {
        val panel = TvSidePanel(this, "Торренты")
        panel.message("Загрузка раздач…")
        var task: kotlinx.coroutines.Job? = null
        panel.addAction("Закрыть") {
            task?.cancel()
            panel.dismiss()
        }
        panel.setOnCancelListener { task?.cancel() }
        panel.show()
        task =
            lifecycleScope.launch {
                try {
                    val items = model.api.releaseTorrents(releaseId)
                    panel.body.removeAllViews()
                    if (items.isEmpty()) panel.message("Раздачи пока недоступны")
                    else
                        items.forEach { item ->
                            panel.addChoice(
                                listOf(item.title, item.description)
                                    .filter { it.isNotBlank() }
                                    .joinToString("\n"),
                                icon = R.drawable.ic_download,
                            ) {
                                openLink(item.magnet)
                            }
                        }
                    panel.body.getFocusables(View.FOCUS_FORWARD).firstOrNull()?.requestFocus()
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    panel.message("Не удалось загрузить раздачи. Проверьте подключение.")
                }
            }
    }

    private fun openSection(kind: HomeSection) {
        rememberFocus()
        model.open(Screen.Section(kind))
    }

    private fun releaseCards(items: List<Release>, key: String) =
        items
            .distinctBy { it.id }
            .map {
                Row(
                    "$key:release:${it.id}",
                    Kind.POSTER,
                    release = it,
                    selected =
                        if (key.startsWith("schedule", true)) 3
                        else if (key.startsWith("latest", true)) 1 else 0,
                )
            }

    private fun watchOrderCards(items: List<Release>, key: String) =
        items
            .distinctBy { it.id }
            .map { item -> Row("$key:${item.id}", Kind.POSTER, release = item, selected = 2) }

    private val franchisePreviewGate = kotlinx.coroutines.sync.Semaphore(2)
    private val franchisePreviews = FranchisePreviewCache()
    private val franchisePreviewErrors = mutableSetOf<String>()
    private val franchisePreviewJobs = mutableMapOf<String, kotlinx.coroutines.Job>()

    private fun loadFranchisePreview(id: String) {
        if (
            franchisePreviews.containsKey(id) ||
                id in franchisePreviewErrors ||
                franchisePreviewJobs[id]?.isActive == true
        )
            return
        franchisePreviewJobs[id] =
            lifecycleScope.launch {
                try {
                    val parts = franchisePreviewGate.withPermit { model.api.franchise(id) }
                    franchisePreviews[id] = parts.map { it.title }
                    franchisePreviewErrors.remove(id)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    franchisePreviewErrors.add(id)
                }
                franchisePreviewJobs.remove(id)
                val screen = model.state.value.screen
                if (
                    screen == Screen.Home ||
                        (screen is Screen.Section && screen.kind == HomeSection.FRANCHISES)
                )
                    render(model.state.value)
            }
    }

    private fun featureCards(items: List<Feature>, kind: HomeSection) =
        items.map { feature ->
            Row(
                "${kind.name}:${feature.id}",
                if (kind == HomeSection.GENRES) Kind.GENRE else Kind.FRANCHISE,
                feature.title,
                feature.subtitle,
                image = feature.image,
                composition = franchisePreviews[feature.id],
                selected = if (feature.id in franchisePreviewErrors) -1 else 0,
                focusLoad =
                    if (kind == HomeSection.FRANCHISES) ({ loadFranchisePreview(feature.id) })
                    else null,
                click = {
                    rememberFocus()
                    if (kind == HomeSection.FRANCHISES)
                        model.open(Screen.Franchise(feature.id, feature.title))
                    else
                        model.open(
                            Screen.Catalog(genre = feature.id.toIntOrNull(), title = feature.title)
                        )
                },
            )
        }

    private fun videoCards(items: List<HomeVideo>) =
        items.map { video ->
            Row(
                "video:${video.id}",
                Kind.FEATURE,
                video.title,
                video.subtitle,
                image = video.image,
                click = {
                    if (video.url.startsWith("https://")) openLink(video.url)
                    else
                        Toast.makeText(
                                this,
                                "У этого ролика нет доступной ссылки",
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                },
            )
        }

    private val pendingShelfRefresh = mutableSetOf<HomeSection>()

    private fun homeShelf(
        kind: HomeSection,
        items: List<Row>,
        wide: Boolean = false,
        key: String = kind.name,
    ): Row {
        val style =
            when (kind) {
                HomeSection.LATEST,
                HomeSection.SCHEDULE -> 4
                HomeSection.GENRES -> 2
                HomeSection.FRANCHISES -> 3
                else -> if (wide) 1 else 0
            }
        val state = model.state.value
        val cards =
            if (kind == HomeSection.GENRES || kind == HomeSection.FRANCHISES)
                discoveryShelfCards(
                    key,
                    kind.title,
                    items,
                    style,
                    kind in state.refreshing,
                    kind in state.refreshErrors,
                    {
                        pendingShelfRefresh.add(kind)
                        model.refreshFeatures(kind)
                    },
                    { openSection(kind) },
                )
            else shelfCards(key, kind.title, items, wide, style) { openSection(kind) }
        return Row("shelf:$key", Kind.SHELF, selected = style, cards = cards)
    }

    private var pendingRender: UiState? = null
    private var renderPosted = false

    private fun render(state: UiState) {
        if (list.isComputingLayout) {
            pendingRender = state
            if (!renderPosted) {
                renderPosted = true
                list.post {
                    renderPosted = false
                    val latest = pendingRender
                    pendingRender = null
                    if (latest != null && !isFinishing && !isDestroyed) render(latest)
                }
            }
            return
        }
        pendingRender = null
        if (scheduleFilterUser != account.session.userId) {
            scheduleFavorites = false
            scheduleCollections = false
            scheduleFilterUser = account.session.userId
        }
        if (
            state.screen !is Screen.Catalog &&
                state.screen !is Screen.Section &&
                !isAccountList(state.screen)
        ) {
            catalogSearchEditing = false
            catalogSearchJob?.cancel()
        }
        (state.screen as? Screen.Detail)?.let { history.restore(it.id) }
        if (current == state.screen && !state.loading && adapter.rows.none { it.key == "loading" })
            rememberFocus()
        val sameDetail =
            (current as? Screen.Detail)?.id == (state.screen as? Screen.Detail)?.id &&
                current is Screen.Detail &&
                adapter.rows.any { it.kind == Kind.HERO }
        val sameCatalog =
            (current is Screen.Catalog || isAccountList(current)) &&
                (current == state.screen ||
                    catalogSearchEditing ||
                    (current is Screen.Collections && state.screen is Screen.Collections)) &&
                adapter.rows.any { it.kind == Kind.CATALOG_BAR }
        val sameSection =
            current is Screen.Section &&
                current == state.screen &&
                adapter.rows.any { it.key == "section-heading" }
        val sameProfile =
            (current == Screen.Profile ||
                current == Screen.AccountSessions ||
                current == Screen.Settings ||
                current is Screen.AccountSettings) &&
                current == state.screen &&
                adapter.rows.any { it.kind in listOf(Kind.ACTION, Kind.SETTING, Kind.ACCOUNT_TILE) }
        val sameAccountHistory =
            current == Screen.AccountHistory &&
                state.screen == Screen.AccountHistory &&
                adapter.rows.any { it.key == "account-history-heading" }
        val sameHome =
            current == Screen.Home &&
                state.screen == Screen.Home &&
                adapter.rows.any { it.kind == Kind.SHELF }
        val collectionTypeChanged =
            current is Screen.Collections &&
                state.screen is Screen.Collections &&
                (current as Screen.Collections).type != state.screen.type
        val retainedFocus =
            if (current == state.screen)
                list.findFocus()?.let { list.findContainingItemView(it)?.tag as? String }
            else null
        if ((current as? Screen.Detail)?.id != (state.screen as? Screen.Detail)?.id) {
            detailFocusToken++
            detailFocusPending = false
            enterContent = false
        }
        if (current != state.screen) {
            remoteClick.cancel()
            remotePendingKey = null
            remoteFocusToken++
            shelfMoveToken++
            pendingShelf = null
        }
        current = state.screen
        val section =
            when (current) {
                Screen.Profile,
                Screen.AccountSessions,
                is Screen.AccountSettings -> 3
                Screen.AccountHistory -> 6
                Screen.Settings -> 7
                is Screen.Favorites -> 4
                is Screen.Collections -> 5
                Screen.Home -> 0
                is Screen.Section ->
                    if ((current as Screen.Section).kind == HomeSection.SCHEDULE) 9 else 1
                is Screen.Catalog,
                is Screen.Franchise -> 1
                is Screen.Library -> 3
                else -> selectedSection
            }
        if (section != selectedSection) {
            selectedSection = section
            buildMenu()
        }
        homeMenuLayout()
        val spans =
            if (
                current == Screen.Profile ||
                    isAccountList(current) ||
                    current is Screen.Catalog ||
                    current is Screen.Section ||
                    current is Screen.Library ||
                    current is Screen.Franchise
            )
                20
            else 15
        if (grid.spanCount != spans) grid.spanCount = spans
        menu.visibility = if (current == Screen.Welcome) View.GONE else View.VISIBLE
        val rows = mutableListOf<Row>()
        val startupVisible =
            state.screen == Screen.Welcome ||
                (state.screen == Screen.Home &&
                    state.home == null &&
                    (state.loading || state.error != null))
        startup.visibility = if (startupVisible) View.VISIBLE else View.GONE
        shell.visibility = if (startupVisible) View.INVISIBLE else View.VISIBLE
        if (startupVisible) startup.show(state.screen == Screen.Welcome, state.error != null)
        if (state.screen == Screen.Welcome) {
            // Welcome has its own centered surface outside the scrolling catalogue.
        } else {

            if (
                state.loading &&
                    state.screen !is Screen.Catalog &&
                    !(state.screen == Screen.Home && state.home != null)
            )
                rows += Row("loading", Kind.TEXT, "Загрузка…")
            else if (state.error != null && state.screen !is Screen.Catalog)
                rows +=
                    Row(
                        "retry",
                        Kind.ACTION,
                        "Не удалось загрузить",
                        state.error,
                        click = model::retry,
                    )
            else
                when (val screen = state.screen) {
                    Screen.Home -> {
                        val home = state.home ?: return

                        rows +=
                            Row(
                                "latest-heading",
                                Kind.HEADING,
                                "Новые эпизоды",
                                selected = 1,
                                click = {
                                    if (!model.state.value.loading) {
                                        rememberFocus()
                                        lastHistoryHydration = 0
                                        hydrateHistory()
                                        model.retry()
                                    }
                                },
                            )
                        rows += homeShelf(HomeSection.LATEST, releaseCards(home.latest, "latest"))
                        val unfinished = history.items()
                        if (unfinished.isNotEmpty()) {
                            rows +=
                                Row(
                                    "continue-heading",
                                    Kind.HEADING,
                                    "Продолжить просмотр",
                                    selected = 1,
                                )
                            val cards =
                                unfinished.map { item ->
                                    val status =
                                        when {
                                            item.nextUp -> "Следующая серия"
                                            item.duration > 0 ->
                                                "Осталось ${(item.remaining+59999)/60000} мин"
                                            else -> "Продолжить"
                                        }
                                    Row(
                                        "continue:${item.releaseId}:${item.episodeId}",
                                        Kind.CONTINUE,
                                        item.releaseTitle,
                                        "Серия ${number(item.ordinal)} · $status",
                                        image = item.preview,
                                        watching = item,
                                        click = { playContinue(item) },
                                        longClick = { continueActions(item) },
                                    )
                                }
                            rows += Row("shelf:continue", Kind.SHELF, selected = 5, cards = cards)
                        }
                        rows +=
                            Row(
                                "schedule-days",
                                Kind.DAYS,
                                "Расписание релизов",
                                selected = state.homeDay,
                            )
                        val schedule = home.schedule?.getOrNull(state.homeDay)
                        if (schedule == null)
                            rows +=
                                Row(
                                    "schedule-retry",
                                    Kind.ACTION,
                                    "Не удалось загрузить расписание · Повторить",
                                    click = model::retry,
                                )
                        else
                            rows +=
                                homeShelf(
                                    HomeSection.SCHEDULE,
                                    releaseCards(schedule, "schedule:${state.homeDay}"),
                                    key = "schedule:${state.homeDay}",
                                )
                        rows += Row("franchises-heading", Kind.HEADING, "Франшизы", selected = 1)
                        rows +=
                            homeShelf(
                                HomeSection.FRANCHISES,
                                featureCards(home.franchises.orEmpty(), HomeSection.FRANCHISES),
                                true,
                            )
                        rows += Row("genres-heading", Kind.HEADING, "Жанры", selected = 1)
                        rows +=
                            homeShelf(
                                HomeSection.GENRES,
                                featureCards(home.genres.orEmpty(), HomeSection.GENRES),
                                true,
                            )
                        rows +=
                            Row("videos-heading", Kind.HEADING, "Видео от AniLiberty", selected = 1)
                        if (home.videos == null)
                            rows +=
                                Row(
                                    "videos-retry",
                                    Kind.ACTION,
                                    "Не удалось загрузить видео · Повторить",
                                    click = model::retry,
                                )
                        else rows += homeShelf(HomeSection.VIDEOS, videoCards(home.videos), true)
                    }
                    is Screen.Section -> {
                        val data = state.section ?: return
                        val count = data.releases.size + data.features.size + data.videos.size
                        rows +=
                            Row(
                                "section-heading",
                                Kind.HEADING,
                                screen.kind.title,
                                selected = 1,
                                click = {
                                    rememberFocus()
                                    model.retry()
                                },
                            )
                        val query = sectionQueries[screen.kind].orEmpty()
                        rows +=
                            Row(
                                "section-tools",
                                Kind.CATALOG_BAR,
                                title = if (catalogSearchEditing) catalogSearchDraft else query,
                                selected = if (catalogSearchEditing) 1 else 0,
                                queryChanged = ::liveCatalogSearch,
                                searchDone = ::finishCatalogSearch,
                                activeControls =
                                    if (screen.kind == HomeSection.SCHEDULE)
                                        buildSet {
                                            if (scheduleFavorites) add(1)
                                            if (scheduleCollections) add(2)
                                        }
                                    else emptySet(),
                                controls =
                                    buildList {
                                        add(
                                            (query.ifBlank { "Поиск в разделе…" }) to
                                                {
                                                    catalogSearchDraft = query
                                                    catalogSearchEditing = true
                                                    render(model.state.value)
                                                }
                                        )
                                        if (screen.kind == HomeSection.SCHEDULE) {
                                            add("Избранное" to { toggleScheduleSource(true) })
                                            add("Коллекции" to { toggleScheduleSource(false) })
                                        }
                                        add(
                                            "Сбросить" to
                                                {
                                                    sectionQueries.remove(screen.kind)
                                                    catalogSearchDraft = ""
                                                    catalogSearchEditing = false
                                                    if (screen.kind == HomeSection.SCHEDULE) {
                                                        scheduleFavorites = false
                                                        scheduleCollections = false
                                                    }
                                                    render(model.state.value)
                                                }
                                        )
                                    },
                            )
                        if (query.isNotBlank() && data.more)
                            rows +=
                                Row(
                                    "section-search-scope",
                                    Kind.TEXT,
                                    "Поиск в загруженной части списка",
                                )
                        if (screen.kind == HomeSection.SCHEDULE) {
                            rows +=
                                Row(
                                    "schedule-week-days",
                                    Kind.ACCOUNT_TABS,
                                    selected = scheduleDay,
                                    controls =
                                        (listOf("Вся неделя") +
                                                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"))
                                            .mapIndexed { i, label ->
                                                label to
                                                    {
                                                        scheduleDay = i
                                                        rememberFocus()
                                                        render(model.state.value)
                                                    }
                                            },
                                )
                        }
                        val source =
                            if (screen.kind == HomeSection.SCHEDULE)
                                data.copy(
                                    releases =
                                        filterScheduleSources(
                                            data.releases,
                                            account.lists?.favorites.orEmpty(),
                                            account.lists?.collections?.keys.orEmpty(),
                                            scheduleFavorites,
                                            scheduleCollections,
                                        )
                                )
                            else data
                        val searched = filterSectionData(source, query)
                        val filtered =
                            if (screen.kind == HomeSection.SCHEDULE && scheduleDay != 0)
                                searched.copy(
                                    releases =
                                        searched.releases.filter { it.publishDay == scheduleDay }
                                )
                            else searched
                        val cards =
                            when (screen.kind) {
                                HomeSection.LATEST,
                                HomeSection.SCHEDULE ->
                                    releaseCards(filtered.releases, screen.kind.name)
                                HomeSection.FRANCHISES,
                                HomeSection.GENRES -> featureCards(filtered.features, screen.kind)
                                HomeSection.VIDEOS -> videoCards(filtered.videos)
                            }
                        if (screen.kind == HomeSection.LATEST) {
                            episodePeriods(filtered.releases).forEach { period ->
                                rows +=
                                    Row(
                                        "period:${period.key}",
                                        Kind.HEADING,
                                        period.title,
                                        selected = 1,
                                    )
                                rows +=
                                    Row(
                                        "period-shelf:${period.key}",
                                        Kind.SHELF,
                                        selected = 6,
                                        cards = releaseCards(period.releases, screen.kind.name),
                                    )
                            }
                        } else if (screen.kind == HomeSection.SCHEDULE) {
                            val groups =
                                if (scheduleDay == 0)
                                    filtered.releases
                                        .groupBy { it.publishDay }
                                        .toSortedMap(compareBy<Int?> { it ?: 8 })
                                else mapOf(scheduleDay to filtered.releases)
                            groups.forEach { (day, items) ->
                                if (items.isNotEmpty()) {
                                    rows +=
                                        Row(
                                            "schedule-day:$day",
                                            Kind.HEADING,
                                            day?.let { scheduleDays.getOrNull(it - 1) }
                                                ?: "День уточняется",
                                            selected = 1,
                                        )
                                    rows +=
                                        Row(
                                            "schedule-day-shelf:$day",
                                            Kind.SHELF,
                                            selected = 6,
                                            cards = releaseCards(items, "schedule:week:$day"),
                                        )
                                }
                            }
                        } else rows += cards.map { it.copy(homeStyle = true) }
                        if (
                            (query.isNotBlank() || screen.kind == HomeSection.LATEST) &&
                                cards.isEmpty() &&
                                state.more &&
                                !state.loadingMore &&
                                state.moreError == null
                        ) {
                            list.post {
                                if (
                                    model.state.value.screen == screen &&
                                        sectionQueries[screen.kind] == query
                                )
                                    model.loadMoreSection()
                            }
                        }
                        if (state.loadingMore)
                            rows += Row("section-loading-more", Kind.TEXT, "Загрузка…")
                        else if (state.moreError != null)
                            rows +=
                                Row(
                                    "section-more-retry",
                                    Kind.ACTION,
                                    "Повторить загрузку",
                                    state.moreError,
                                    click = model::loadMoreSection,
                                )
                        if (cards.isEmpty())
                            rows +=
                                Row(
                                    "section-empty",
                                    Kind.TEXT,
                                    if (query.isNotBlank()) "Ничего не найдено"
                                    else "Пока ничего нет",
                                )
                    }

                    Screen.Profile -> {
                        rows +=
                            Row(
                                "profile-heading",
                                Kind.HEADING,
                                "Профиль",
                                selected = 1,
                                click =
                                    if (account.signedIn) ({ refreshAccount(force = true) })
                                    else null,
                            )
                        rows +=
                            Row(
                                "profile-identity",
                                Kind.PROFILE,
                                profile = account.session.profile(),
                            )
                        if (account.signedIn) {
                            profileError?.let {
                                rows +=
                                    Row(
                                        "profile-error",
                                        Kind.TEXT,
                                        "Не удалось синхронизировать",
                                        it,
                                    )
                            }
                            if (account.session.profile()?.banned == true)
                                rows +=
                                    Row(
                                        "profile-ban",
                                        Kind.TEXT,
                                        "Аккаунт ограничен",
                                        "Подробности доступны в профиле на сайте.",
                                    )
                            rows +=
                                Row(
                                    "profile-data",
                                    Kind.ACCOUNT_TILE,
                                    "Данные аккаунта",
                                    "Имя, логин и безопасность",
                                    selected = R.drawable.ic_account,
                                    click = { model.open(Screen.AccountSettings("identity")) },
                                )
                            rows +=
                                Row(
                                    "profile-sessions",
                                    Kind.ACCOUNT_TILE,
                                    "Устройства",
                                    "Активные сеансы",
                                    selected = R.drawable.ic_settings,
                                    click = {
                                        rememberFocus()
                                        model.open(Screen.AccountSessions)
                                        loadAccountSessions()
                                    },
                                )
                            val queued = pending(prefs).size
                            rows +=
                                Row(
                                    "profile-sync",
                                    Kind.ACCOUNT_TILE,
                                    "Синхронизация",
                                    if (queued > 0) "Не отправлено: $queued серий"
                                    else "Прогресс и локальная история",
                                    selected = R.drawable.ic_refresh,
                                    click = { model.open(Screen.AccountSettings("sync")) },
                                )
                            rows +=
                                Row(
                                    "profile-settings",
                                    Kind.ACCOUNT_TILE,
                                    "Настройки",
                                    "Плеер, реклама и меню",
                                    selected = R.drawable.ic_settings,
                                    click = { model.open(Screen.Settings) },
                                )
                            rows +=
                                Row(
                                    "profile-account",
                                    Kind.SETTING,
                                    "Вход и выход",
                                    "Сменить аккаунт или выйти на этом ТВ",
                                    favoriteTitle = "›",
                                    click = {
                                        TvSidePanel(this, "Аккаунт").apply {
                                            addAction("Сменить аккаунт") {
                                                dismiss()
                                                login()
                                            }
                                            addAction("Выйти") {
                                                dismiss()
                                                confirmLogout()
                                            }
                                            addAction("Отмена") { dismiss() }
                                            show()
                                        }
                                    },
                                )
                        } else {
                            rows +=
                                Row(
                                    "profile-guest",
                                    Kind.TEXT,
                                    "Избранное, коллекции и история сохраняются на этом ТВ",
                                )
                            rows +=
                                Row(
                                    "profile-login",
                                    Kind.SETTING,
                                    "Войти в аккаунт",
                                    "",
                                    favoriteTitle = "›",
                                    click = { login() },
                                )
                            rows +=
                                Row(
                                    "profile-local",
                                    Kind.ACCOUNT_TILE,
                                    "На этом ТВ",
                                    "Избранное",
                                    selected = R.drawable.ic_favorite,
                                    click = { localLibrary() },
                                )
                            rows +=
                                Row(
                                    "profile-local-collections",
                                    Kind.ACCOUNT_TILE,
                                    "Коллекции",
                                    selected = R.drawable.ic_library,
                                    click = { openAccountList(Screen.Collections()) },
                                )
                            rows +=
                                Row(
                                    "profile-local-history",
                                    Kind.ACCOUNT_TILE,
                                    "История просмотра",
                                    selected = R.drawable.ic_collection_history,
                                    click = { openAccountHistory() },
                                )
                            rows +=
                                Row(
                                    "profile-settings",
                                    Kind.ACCOUNT_TILE,
                                    "Настройки",
                                    "Плеер и меню",
                                    selected = R.drawable.ic_settings,
                                    click = { model.open(Screen.Settings) },
                                )
                        }
                    }
                    is Screen.AccountSettings -> {
                        val profile = account.session.profile()
                        rows +=
                            Row(
                                "account-settings-heading",
                                Kind.HEADING,
                                if (screen.section == "identity") "Данные аккаунта"
                                else "Синхронизация",
                                selected = 1,
                            )
                        if (!account.signedIn)
                            rows +=
                                Row(
                                    "account-settings-login",
                                    Kind.SETTING,
                                    "Войти в аккаунт",
                                    favoriteTitle = "›",
                                    click = { login() },
                                )
                        else if (screen.section == "identity") {
                            rows +=
                                Row(
                                    "account-nickname",
                                    Kind.SETTING,
                                    "Имя",
                                    favoriteTitle = profile?.name.orEmpty(),
                                    click = { editAccountIdentity(true) },
                                )
                            rows +=
                                Row(
                                    "account-login",
                                    Kind.SETTING,
                                    "Логин",
                                    favoriteTitle = profile?.login.orEmpty(),
                                    click = { editAccountIdentity(false) },
                                )
                            rows += Row("account-security-heading", Kind.HEADING, "Безопасность")
                            rows +=
                                Row(
                                    "account-email",
                                    Kind.SETTING,
                                    "Почта",
                                    profile?.email.orEmpty(),
                                    favoriteTitle = "На телефоне ↗",
                                    click = { accountSettingsOnPhone() },
                                )
                            rows +=
                                Row(
                                    "account-avatar",
                                    Kind.SETTING,
                                    "Аватар и пароль",
                                    "Изменить на официальном сайте",
                                    favoriteTitle = "На телефоне ↗",
                                    click = { accountSettingsOnPhone() },
                                )
                        } else {
                            val queued = pending(prefs).size
                            rows +=
                                Row(
                                    "account-sync-status",
                                    Kind.TEXT,
                                    if (profileSyncing) "Синхронизация…"
                                    else if (queued > 0) "Ожидают отправки: $queued серий"
                                    else "Всё синхронизировано",
                                    "Избранное, коллекции и история гостя остаются на ТВ. Перенос в аккаунт — только по вашему выбору.",
                                )
                            profileError?.let {
                                rows +=
                                    Row("account-sync-error", Kind.TEXT, "Ошибка соединения", it)
                            }
                            rows +=
                                Row(
                                    "account-sync-now",
                                    Kind.SETTING,
                                    "Синхронизировать",
                                    "",
                                    favoriteTitle = "↻",
                                    click = { refreshAccount(force = true) },
                                )
                            rows +=
                                Row(
                                    "account-sync-import",
                                    Kind.SETTING,
                                    "Перенести данные с ТВ",
                                    "Проверить серии перед отправкой в аккаунт",
                                    favoriteTitle = "›",
                                    click = { offerLocalHistory() },
                                )
                        }
                    }
                    Screen.Settings -> {
                        rows += Row("settings-heading", Kind.HEADING, "Настройки", selected = 1)
                        fun localToggle(
                            key: String,
                            title: String,
                            description: String,
                            default: Boolean = true,
                        ) {
                            rows +=
                                Row(
                                    "setting:$key",
                                    Kind.SETTING,
                                    title,
                                    description,
                                    favoriteTitle =
                                        if (settings.enabled(key, default)) "Вкл. ●" else "Выкл. ○",
                                    click = {
                                        settings.toggle(key, default)
                                        rememberFocus()
                                        buildMenu()
                                        render(model.state.value)
                                    },
                                )
                        }
                        rows += Row("settings-player", Kind.HEADING, "Воспроизведение")
                        localToggle("skip-opening", "Автопропуск опенингов", "", false)
                        localToggle("skip-ending", "Автопропуск эндингов", "", false)
                        localToggle(
                            "auto-next",
                            "Следующий эпизод автоматически",
                            "",
                            prefs.getBoolean("auto-next", true),
                        )
                        rows += Row(
                            "setting:watched-percent",
                            Kind.SETTING,
                            "Считать серию просмотренной после",
                            "Доля длительности серии для автоматической отметки",
                            favoriteTitle = "${settings.watchedPercent}%",
                            click = {
                                showWatchedPercent(this) {
                                    rememberFocus()
                                    render(model.state.value)
                                }
                            },
                        )
                        rows += Row("settings-ads", Kind.HEADING, "Реклама на сайте AniLiberty")
                        if (account.signedIn) {
                            val profile = account.session.profile()
                            listOf(
                                    Triple("video", "Показывать видеорекламу", profile?.videoAds),
                                    Triple(
                                        "always",
                                        "Реклама перед каждым эпизодом",
                                        profile?.videoAdsAlways,
                                    ),
                                    Triple(
                                        "content",
                                        "Показывать контентную рекламу",
                                        profile?.contentAds,
                                    ),
                                )
                                .forEach { (key, title, value) ->
                                    rows +=
                                        Row(
                                            "setting-ad:$key",
                                            Kind.SETTING,
                                            title,
                                            favoriteTitle =
                                                when (value) {
                                                    true -> "Вкл. ●"
                                                    false -> "Выкл. ○"
                                                    null -> "—"
                                                },
                                            click = {
                                                accountAction {
                                                    account.refresh()
                                                    if (value != null) {
                                                        val fresh = account.session.profile()
                                                        val actual =
                                                            when (key) {
                                                                "video" -> fresh?.videoAds
                                                                "always" -> fresh?.videoAdsAlways
                                                                else -> fresh?.contentAds
                                                            }
                                                        if (actual == value)
                                                            account.toggleAdPreference(key)
                                                    }
                                                }
                                            },
                                        )
                                }
                        } else
                            rows +=
                                Row(
                                    "settings-ads-login",
                                    Kind.ACTION,
                                    "Войти для настройки рекламы",
                                    click = { login() },
                                )
                        rows += Row("settings-menu", Kind.HEADING, "Кнопки бокового меню")
                        localToggle("menu-random", "Случайный релиз", "", false)
                        localToggle("menu-search", "Поиск", "")
                        localToggle("menu-favorites", "Избранное", "")
                        localToggle("menu-collections", "Коллекции", "")
                        localToggle("menu-settings", "Настройки", "")
                    }
                    is Screen.Favorites,
                    is Screen.Collections -> {
                        val title = if (screen is Screen.Favorites) "Избранное" else "Коллекции"
                        rows +=
                            Row(
                                "account-list-heading",
                                Kind.HEADING,
                                title,
                                selected = 1,
                                click =
                                    if (account.signedIn)
                                        ({
                                            loadAccountReleases(screen, true)
                                            render(model.state.value)
                                        })
                                    else null,
                            )
                        run {
                            if (screen is Screen.Collections) {
                                rows +=
                                    Row(
                                        "account-collections-tabs",
                                        Kind.ACCOUNT_TABS,
                                        selected = CollectionType.entries.indexOf(screen.type),
                                        controls =
                                            CollectionType.entries.map { type ->
                                                type.title to
                                                    {
                                                        model.open(screen.copy(type = type), false)
                                                    }
                                            },
                                    )
                            }
                            rows +=
                                Row(
                                    "account-list-tools",
                                    Kind.CATALOG_BAR,
                                    title = catalogSearchDraft,
                                    selected = if (catalogSearchEditing) 1 else 0,
                                    queryChanged = ::liveCatalogSearch,
                                    searchDone = ::finishCatalogSearch,
                                    controls =
                                        buildList {
                                            add(
                                                (accountListQuery(screen).ifBlank { "Поиск…" }) to
                                                    {
                                                        catalogSearchDraft =
                                                            accountListQuery(screen)
                                                        catalogSearchEditing = true
                                                        render(model.state.value)
                                                    }
                                            )
                                            add("Фильтры" to { accountFilters(screen) })
                                            if (screen is Screen.Favorites)
                                                add(
                                                    "Сортировка" to { accountFilters(screen, true) }
                                                )
                                            add(
                                                "Сбросить" to
                                                    {
                                                        model.open(
                                                            if (screen is Screen.Collections)
                                                                Screen.Collections(screen.type)
                                                            else Screen.Favorites(),
                                                            false,
                                                        )
                                                    }
                                            )
                                        },
                                )
                            if (
                                accountReleaseScreen != screen ||
                                    accountReleaseUser != account.session.userId
                            ) {
                                list.post {
                                    if (current == screen) loadAccountReleases(screen, true)
                                }
                                rows += Row("account-list-loading", Kind.TEXT, "Загрузка…")
                            } else {
                                rows +=
                                    accountReleases.items.map {
                                        Row(
                                            "release:${it.id}",
                                            Kind.POSTER,
                                            release = it,
                                            selected = 2,
                                        )
                                    }
                                if (accountReleases.loading)
                                    rows += Row("account-list-loading", Kind.TEXT, "Загрузка…")
                                else if (accountReleases.error != null)
                                    rows +=
                                        Row(
                                            "account-list-error",
                                            Kind.ACTION,
                                            "Повторить загрузку",
                                            accountReleases.error.orEmpty(),
                                            click = { loadAccountReleases(screen) },
                                        )
                                else if (accountReleases.items.isEmpty())
                                    rows += Row("account-list-empty", Kind.TEXT, "Список пуст")
                                else if (accountReleases.more)
                                    rows +=
                                        Row(
                                            "account-list-more",
                                            Kind.ACTION,
                                            "Загрузить ещё",
                                            click = { loadAccountReleases(screen) },
                                        )
                            }
                        }
                    }
                    Screen.AccountSessions -> {
                        rows +=
                            Row(
                                "sessions-heading",
                                Kind.HEADING,
                                "Устройства",
                                selected = 1,
                                click = { loadAccountSessions() },
                            )
                        if (accountSessionsLoading)
                            rows += Row("sessions-loading", Kind.TEXT, "Загрузка устройств…")
                        accountSessions.forEach { session ->
                            if (session.current)
                                rows +=
                                    Row(
                                        "session:${session.id}",
                                        Kind.TEXT,
                                        "${session.title} · Этот ТВ",
                                        session.description,
                                    )
                            else
                                rows +=
                                    Row(
                                        "session:${session.id}",
                                        Kind.ACTION,
                                        session.title,
                                        session.description,
                                        click = {
                                            TvSidePanel(this, "Завершить сеанс?").apply {
                                                message(session.title + "\n" + session.description)
                                                addAction("Завершить") {
                                                    dismiss()
                                                    accountAction(
                                                        done = { loadAccountSessions() }
                                                    ) {
                                                        account.deleteSession(session.id)
                                                    }
                                                }
                                                addAction("Отмена") { dismiss() }
                                                show()
                                            }
                                        },
                                    )
                        }
                        if (accountSessionsError != null)
                            rows +=
                                Row(
                                    "sessions-error",
                                    Kind.TEXT,
                                    "Не удалось обновить",
                                    accountSessionsError.orEmpty(),
                                )
                        if (accountSessions.any { !it.current })
                            rows +=
                                Row(
                                    "sessions-others",
                                    Kind.ACTION,
                                    "Завершить все другие сеансы",
                                    click = {
                                        TvSidePanel(this, "Завершить другие сеансы?").apply {
                                            message(
                                                "Потребуется заново войти на остальных устройствах. Этот ТВ останется в аккаунте."
                                            )
                                            addAction("Завершить") {
                                                dismiss()
                                                accountAction(done = { loadAccountSessions() }) {
                                                    account.deleteOtherSessions()
                                                }
                                            }
                                            addAction("Отмена") { dismiss() }
                                            show()
                                        }
                                    },
                                )
                    }
                    Screen.AccountHistory -> {
                        rows +=
                            Row(
                                "account-history-heading",
                                Kind.HEADING,
                                "История просмотра",
                                selected = 1,
                                click = { loadAccountHistory(reset = true) },
                            )
                        rows +=
                            accountHistoryItems.map { item ->
                                val duration = (item.episode.duration * 1000).coerceAtLeast(1)
                                val position =
                                    if (item.time.watched) duration
                                    else (item.time.seconds * 1000).toLong()
                                Row(
                                    "account-history:${item.episode.id}",
                                    Kind.EPISODE,
                                    title = item.release.title,
                                    release = item.release,
                                    episode = item.episode,
                                    episodeProgress = position to duration,
                                    click = {
                                        rememberFocus()
                                        startActivity(
                                            Intent(this, PlayerActivity::class.java)
                                                .putExtra("release", item.release.id)
                                                .putExtra("episode", item.episode.id)
                                        )
                                    },
                                )
                            }
                        if (accountHistoryLoading)
                            rows += Row("account-history-loading", Kind.TEXT, "Загрузка истории…")
                        else if (accountHistoryError != null)
                            rows +=
                                Row(
                                    "account-history-retry",
                                    Kind.ACTION,
                                    "Повторить загрузку",
                                    accountHistoryError.orEmpty(),
                                    click = { loadAccountHistory(reset = accountHistoryPage == 0) },
                                )
                        else if (accountHistoryItems.isEmpty())
                            rows +=
                                Row("account-history-empty", Kind.TEXT, "Вы ещё ничего не смотрели")
                        else if (accountHistoryMore)
                            rows +=
                                Row(
                                    "account-history-more",
                                    Kind.ACTION,
                                    "Загрузить ещё",
                                    click = { loadAccountHistory() },
                                )
                    }
                    is Screen.Library -> {
                        rows += Row("library-heading", Kind.HEADING, screen.title, selected = 1)
                        rows +=
                            state.releases.map {
                                Row("library:${it.id}", Kind.POSTER, release = it, selected = 2)
                            }
                        if (state.releases.isEmpty()) {
                            rows +=
                                Row(
                                    "library-empty",
                                    Kind.TEXT,
                                    "Здесь будут ваши тайтлы",
                                    if (account.signedIn)
                                        "Выбирайте избранное и коллекцию на странице тайтла."
                                    else "Сохраняйте тайтлы на этом ТВ со страницы тайтла.",
                                )
                            rows +=
                                Row(
                                    "library-catalogue",
                                    Kind.ACTION,
                                    "Открыть каталог",
                                    click = { model.open(Screen.Catalog()) },
                                )
                        }
                    }
                    is Screen.Franchise -> {
                        rows +=
                            Row(
                                "franchise-heading",
                                Kind.HEADING,
                                screen.title,
                                "Порядок просмотра · слева направо",
                            )
                        if (state.releases.isNotEmpty())
                            rows +=
                                Row(
                                    "franchise-order:${screen.id}",
                                    Kind.SHELF,
                                    selected = 7,
                                    cards =
                                        watchOrderCards(state.releases, "franchise:${screen.id}"),
                                )
                        if (state.releases.isEmpty())
                            rows += Row("empty", Kind.TEXT, "Релизы пока не указаны")
                    }
                    is Screen.Catalog -> {
                        val total =
                            state.catalogTotal?.let { "Найдено: $it" }
                                ?: "Загружено: ${state.releases.size}"
                        rows +=
                            Row(
                                "catalogue-heading",
                                Kind.HEADING,
                                screen.title ?: "Каталог",
                                total,
                                selected = 1,
                            )
                        val sort =
                            catalogReferences["sorting"]
                                ?.firstOrNull { it.value == screen.filters.sorting }
                                ?.title
                                ?: when (screen.filters.sorting) {
                                    "RATING_DESC" -> "По рейтингу"
                                    "YEAR_DESC" -> "Сначала новые"
                                    "YEAR_ASC" -> "Сначала старые"
                                    else -> "Сортировка"
                                }
                        rows +=
                            Row(
                                "catalogue-tools",
                                Kind.CATALOG_BAR,
                                title = catalogSearchDraft,
                                selected = if (catalogSearchEditing) 1 else 0,
                                queryChanged = ::liveCatalogSearch,
                                searchDone = ::finishCatalogSearch,
                                controls =
                                    listOf(
                                        (if (screen.query.isBlank()) "Поиск по каталогу…"
                                        else screen.query.take(40)) to { catalogSearch(screen) },
                                        "Фильтры${if(screen.filters.count+(if(screen.genre!=null)1 else 0)>0)" · ${screen.filters.count+(if(screen.genre!=null)1 else 0)}"else ""}" to
                                            {
                                                catalogFilters(screen)
                                            },
                                        sort to { catalogSorting(screen) },
                                        "Сбросить" to
                                            {
                                                rememberFocus()
                                                model.open(Screen.Catalog(), false)
                                            },
                                    ),
                            )
                        if (
                            screen.filters.count > 0 ||
                                screen.genre != null ||
                                screen.query.isNotBlank()
                        )
                            rows += Row("catalogue-active", Kind.TEXT, catalogSummary(screen))
                        rows +=
                            state.releases.map {
                                Row("release:${it.id}", Kind.POSTER, release = it, selected = 2)
                            }
                        if (state.loading)
                            rows += Row("catalogue-search-loading", Kind.TEXT, "Поиск…")
                        else if (state.error != null)
                            rows +=
                                Row(
                                    "catalogue-search-retry",
                                    Kind.ACTION,
                                    "Повторить поиск",
                                    state.error,
                                    click = model::retry,
                                )
                        else if (state.releases.isEmpty())
                            rows +=
                                Row(
                                    "catalogue-empty",
                                    Kind.ACTION,
                                    "Ничего не найдено",
                                    "Измените запрос или фильтры",
                                    click = { catalogFilters(screen) },
                                )
                        if (state.loadingMore)
                            rows += Row("catalogue-loading-more", Kind.TEXT, "Загрузка…")
                        else if (state.moreError != null)
                            rows +=
                                Row(
                                    "catalogue-more-retry",
                                    Kind.ACTION,
                                    "Повторить загрузку",
                                    state.moreError,
                                    click = model::loadMoreCatalog,
                                )
                    }
                    is Screen.Detail -> {
                        val r = state.detail ?: return
                        account.applyProgress(r)
                        val description =
                            android.text.Html.fromHtml(
                                    r.description,
                                    android.text.Html.FROM_HTML_MODE_LEGACY,
                                )
                                .toString()
                                .trim()
                        rows +=
                            Row(
                                "hero",
                                Kind.HERO,
                                release = r,
                                selected = if (isFavorite(r.id)) 1 else 0,
                                favoriteTitle =
                                    if (isFavorite(r.id)) "В избранном" else "В избранное",
                                controls =
                                    buildList {
                                        add(
                                            (account.lists?.collections?.get(r.id)?.title
                                                ?: "Коллекция") to { collections(r) }
                                        )
                                        if (description.isNotBlank())
                                            add(
                                                "Описание" to
                                                    {
                                                        TvSidePanel(this@MainActivity, "Описание")
                                                            .apply {
                                                                message(description)
                                                                addAction("Закрыть") { dismiss() }
                                                                show()
                                                            }
                                                        Unit
                                                    }
                                            )
                                        add("Ещё" to { showDetailMore(r) })
                                    },
                            )
                        if (r.blocked)
                            rows +=
                                Row(
                                    "release-restriction",
                                    Kind.TEXT,
                                    "Просмотр ограничен правообладателем или регионом",
                                )
                        rows +=
                            Row(
                                "tabs",
                                Kind.TABS,
                                title = r.episodes.size.toString(),
                                selected = screen.tab,
                            )
                        when (screen.tab) {
                            0 -> {
                                rows +=
                                    r.episodes.map {
                                        Row(
                                            "episode:${it.id}",
                                            Kind.EPISODE,
                                            episode = it,
                                            episodeProgress =
                                                prefs.getLong("position:${it.id}", 0) to
                                                    prefs.getLong("duration:${it.id}", 0),
                                            longClick = { episodeActions(r, it) },
                                        )
                                    }
                                if (r.episodes.isEmpty())
                                    rows += Row("empty", Kind.TEXT, "Эпизоды пока недоступны")
                            }
                            1 -> {
                                val franchises = state.discovery?.franchises
                                when {
                                    state.discoveryLoading && franchises == null ->
                                        rows +=
                                            Row("related-loading", Kind.TEXT, "Загрузка франшизы…")
                                    franchises == null ->
                                        rows +=
                                            Row(
                                                "related-retry",
                                                Kind.ACTION,
                                                "Не удалось загрузить франшизу · Повторить",
                                                click = model::retryDiscovery,
                                            )
                                    franchises.isEmpty() ->
                                        rows +=
                                            Row(
                                                "related-empty",
                                                Kind.TEXT,
                                                "Релиз не связан ни с одной франшизой",
                                            )
                                    else ->
                                        franchises.forEach { group ->
                                            rows +=
                                                Row(
                                                    "franchise-heading:${group.id}",
                                                    Kind.HEADING,
                                                    group.title.ifBlank { "Связанные релизы" },
                                                    "Порядок просмотра · слева направо",
                                                )
                                            rows +=
                                                Row(
                                                    "franchise-order:${group.id}",
                                                    Kind.SHELF,
                                                    selected = 7,
                                                    cards =
                                                        watchOrderCards(
                                                            group.releases,
                                                            "related:${group.id}",
                                                        ),
                                                )
                                        }
                                }
                            }
                            2 -> {
                                val similar =
                                    state.discovery?.recommendations?.let {
                                        uniqueRecommendations(
                                            r.id,
                                            it,
                                            state.discovery.franchises.orEmpty(),
                                        )
                                    }
                                when {
                                    state.discoveryLoading && similar == null ->
                                        rows +=
                                            Row("similar-loading", Kind.TEXT, "Подбираем аниме…")
                                    similar == null ->
                                        rows +=
                                            Row(
                                                "similar-retry",
                                                Kind.ACTION,
                                                "Повторить загрузку рекомендаций",
                                                click = model::retryDiscovery,
                                            )
                                    similar.isEmpty() ->
                                        rows +=
                                            Row(
                                                "similar-empty",
                                                Kind.TEXT,
                                                "Рекомендации пока отсутствуют",
                                            )
                                    else ->
                                        rows +=
                                            Row(
                                                "similar-shelf:${r.id}",
                                                Kind.SHELF,
                                                selected = 7,
                                                cards = watchOrderCards(similar, "similar:${r.id}"),
                                            )
                                }
                            }
                            3 -> {
                                rows +=
                                    r.credits
                                        .groupBy { it.role }
                                        .map { (role, people) ->
                                            Row(
                                                "role:$role",
                                                Kind.TEXT,
                                                role,
                                                people.joinToString(" · ") { it.name },
                                            )
                                        }
                                if (r.credits.isEmpty())
                                    rows += Row("empty", Kind.TEXT, "Участники пока не указаны")
                            }
                        }
                    }
                    else -> Unit
                }
        }
        if (current == Screen.Home) {
            val completed = pendingShelfRefresh.filter { it !in state.refreshing }
            val focusedShelf = list.findFocus()?.let { list.findContainingItemView(it)?.tag }
            completed.forEach { kind ->
                if (kind !in state.refreshErrors)
                    adapter.resetShelf(
                        "shelf:${kind.name}",
                        !menuOpen && focusedShelf == "shelf:${kind.name}",
                    )
                pendingShelfRefresh.remove(kind)
            }
        }
        val preloadFranchises =
            when (val screen = state.screen) {
                Screen.Home -> state.home?.franchises.orEmpty()
                is Screen.Section ->
                    if (screen.kind == HomeSection.FRANCHISES) state.section?.features.orEmpty()
                    else emptyList()
                else -> emptyList()
            }
        preloadFranchises.take(12).forEach { loadFranchisePreview(it.id) }
        val keepDetailTabFocus =
            sameDetail &&
                current is Screen.Detail &&
                !enterContent &&
                list.findFocus()?.let { list.findContainingItemView(it)?.tag } == "tabs"
        adapter.submit(withFavoriteState(rows))
        if (collectionTypeChanged && !menuOpen) {
            val tabPosition = rows.indexOfFirst { it.key == "account-collections-tabs" }
            val selectedType = (state.screen as Screen.Collections).type
            if (tabPosition >= 0) {
                grid.scrollToPositionWithOffset(tabPosition, list.dp(12))
                list.doOnNextLayout {
                    if (current != state.screen || menuOpen) return@doOnNextLayout
                    val tabs =
                        list.findViewHolderForAdapterPosition(tabPosition)?.itemView
                            as? android.widget.HorizontalScrollView
                    val line = tabs?.getChildAt(0) as? LinearLayout
                    line?.getChildAt(CollectionType.entries.indexOf(selectedType))?.requestFocus()
                }
                list.requestLayout()
            }
            return
        }
        if (startupVisible) return
        if (state.screen is Screen.Detail && !state.loading && rows.any { it.kind == Kind.HERO }) {
            if (menuOpen || !list.hasWindowFocus()) return
            if (enterContent) {
                enterContent = false
                enterDetailContent()
                return
            }
            if (keepDetailTabFocus) return
            if (
                sameDetail &&
                    detailAddress()?.let { address -> rows.any { it.key == address.row } } == true
            )
                return
            val screen = state.screen
            val bookmark = detailBookmarks[screen.id to screen.tab]
            val index =
                rows.indexOfFirst { it.key == bookmark?.row }.takeIf { it >= 0 }
                    ?: rows.indexOfFirst { it.kind == Kind.HERO }
            if (index >= 0)
                focusDetail(
                    index,
                    bookmark?.child
                        ?: if (rows[index].kind == Kind.HERO) "hero-action:${R.drawable.ic_play}"
                        else null,
                )
            return
        }
        if (keepDetailTabFocus) return
        if (menuOpen) return
        if (
            catalogSearchEditing &&
                (current is Screen.Catalog || current is Screen.Section || isAccountList(current))
        )
            return
        if (
            enterCatalogResults &&
                (current is Screen.Catalog ||
                    current is Screen.Section ||
                    isAccountList(current)) &&
                !state.loading
        ) {
            enterCatalogResults = false
            val index =
                rows
                    .indexOfFirst {
                        it.kind in
                            listOf(
                                Kind.POSTER,
                                Kind.GENRE,
                                Kind.FRANCHISE,
                                Kind.FEATURE,
                                Kind.SHELF,
                            )
                    }
                    .takeIf { it >= 0 } ?: rows.indexOfFirst { it.kind == Kind.CATALOG_BAR }
            if (index >= 0) {
                list.doOnNextLayout {
                    focusItem(list.findViewHolderForAdapterPosition(index)?.itemView)
                }
                grid.scrollToPositionWithOffset(index, list.dp(96))
            }
        } else if (enterDay) {
            enterDay = false
            val index =
                rows.indexOfFirst { it.key.startsWith("shelf:schedule:") }.takeIf { it >= 0 }
                    ?: rows.indexOfFirst { it.key == "schedule-days" }
            if (index >= 0) {
                val days = rows.indexOfFirst { it.key == "schedule-days" }
                list.doOnNextLayout {
                    val bar =
                        list.findViewHolderForAdapterPosition(days)?.itemView
                            as? android.widget.HorizontalScrollView
                    val line = bar?.getChildAt(0) as? LinearLayout
                    line?.getChildAt(state.homeDay + 1)?.requestFocus()
                }
            }
        } else if (enterContent && adapter.contentStart in rows.indices) {
            enterContent = false
            val index =
                (adapter.contentStart until rows.size).firstOrNull { rows[it].kind != Kind.HEADING }
                    ?: adapter.contentStart
            list.doOnNextLayout {
                list.findViewHolderForAdapterPosition(index)?.itemView?.requestFocus()
            }
            grid.scrollToPositionWithOffset(index, list.dp(64))
        } else {
            val key = retainedFocus ?: focusMemory[screenKey(state.screen)]
            val index =
                rows.indexOfFirst { it.key == key }.takeIf { it >= 0 }
                    ?: if (key != null && adapter.contentStart >= 0)
                        (adapter.contentStart until rows.size).firstOrNull {
                            rows[it].kind != Kind.HEADING
                        } ?: adapter.contentStart
                    else
                        rows.indexOfFirst {
                            it.kind in
                                listOf(
                                    Kind.ACTION,
                                    Kind.SETTING,
                                    Kind.ACCOUNT_TILE,
                                    Kind.POSTER,
                                    Kind.HERO,
                                    Kind.SHELF,
                                    Kind.FEATURE,
                                    Kind.GENRE,
                                    Kind.FRANCHISE,
                                    Kind.CONTINUE,
                                    Kind.MORE,
                                    Kind.CATALOG_BAR,
                                )
                        }
            if (index >= 0 && !state.loading) {
                list.doOnNextLayout {
                    if (current != state.screen || menuOpen) return@doOnNextLayout
                    val item = list.findViewHolderForAdapterPosition(index)?.itemView
                    if (
                        (!sameHome &&
                            !sameCatalog &&
                            !sameSection &&
                            !sameProfile &&
                            !sameAccountHistory) || list.findFocus() == null
                    )
                        focusItem(item)
                }
                val scroll = scrollMemory[screenKey(state.screen)]
                val anchor =
                    scroll?.first?.let { saved ->
                        rows.indexOfFirst { it.key == saved }.takeIf { it >= 0 }
                    }
                if (
                    !sameHome && !sameCatalog && !sameSection && !sameProfile && !sameAccountHistory
                )
                    grid.scrollToPositionWithOffset(
                        anchor ?: if (key == null) 0 else index,
                        scroll?.second ?: if (key == null) 0 else list.dp(12),
                    )
            }
        }
    }
}
