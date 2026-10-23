package tv.aniliberty.next

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.allowHardware
import coil3.request.transformations

const val RED = 0xFFFE3635.toInt()
const val WHITE = 0xFFFFFFFF.toInt()
const val MUTED = 0xFFB4B4B4.toInt()

fun View.dp(n: Int): Int = (n * resources.displayMetrics.density).toInt()

fun shape(color: Int, radius: Float = 6f, stroke: Int = 0, strokeWidth: Int = 2): GradientDrawable =
    GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
        if (stroke != 0) setStroke(strokeWidth, stroke)
    }

fun View.focusBackground(primary: Boolean = false, radiusDp: Int = 6) {
    background =
        StateListDrawable().apply {
            addState(
                intArrayOf(android.R.attr.state_focused),
                shape(
                    if (primary) WHITE else 0xFF292929.toInt(),
                    dp(radiusDp).toFloat(),
                    if (primary) 0 else WHITE,
                    dp(2),
                ),
            )
            addState(
                intArrayOf(),
                shape(if (primary) RED else Color.TRANSPARENT, dp(radiusDp).toFloat()),
            )
        }
}

/** Shared web catalogue/home control surface; focus never changes geometry. */
fun View.webControlBackground(selected: Boolean = false) {
    background =
        StateListDrawable().apply {
            addState(
                intArrayOf(android.R.attr.state_focused),
                shape(0xFF292929.toInt(), dp(6).toFloat(), WHITE, dp(2)),
            )
            addState(
                intArrayOf(),
                shape(
                    if (selected) 0xFF352323.toInt() else 0xFF212121.toInt(),
                    dp(6).toFloat(),
                    if (selected) RED else 0,
                    dp(1),
                ),
            )
        }
}

fun TextView.label(value: String, size: Float, color: Int = WHITE, bold: Boolean = false) {
    text = value
    textSize = size
    setTextColor(color)
    typeface = resources.getFont(if (bold) R.font.montserrat else R.font.inter)
}

fun action(
    parent: ViewGroup,
    title: String,
    primary: Boolean = false,
    icon: Int = 0,
    click: () -> Unit,
): TextView =
    TextView(parent.context).apply {
        label(title, 16f, WHITE, true)
        gravity = Gravity.CENTER
        isFocusable = true
        isClickable = true
        minimumHeight = dp(46)
        setPadding(dp(16), 0, dp(16), 0)
        if (primary) focusBackground(true) else webControlBackground()
        setTextColor(
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()),
                intArrayOf(if (primary) Color.BLACK else WHITE, WHITE),
            )
        )
        if (icon != 0) {
            val drawable =
                context.getDrawable(icon)!!.mutate().apply {
                    setBounds(0, 0, dp(20), dp(20))
                    setTintList(textColors)
                }
            setCompoundDrawablesRelative(drawable, null, null, null)
            compoundDrawablePadding = dp(8)
        }
        setOnClickListener { click() }
    }

enum class Kind {
    SETTING,
    ACCOUNT_TILE,
    ACCOUNT_TABS,
    PROFILE,
    HERO,
    TABS,
    POSTER,
    EPISODE,
    HEADING,
    TEXT,
    ACTION,
    FEATURE,
    DAYS,
    SHELF,
    MORE,
    GENRE,
    FRANCHISE,
    REFRESH,
    CONTINUE,
    CATALOG_BAR,
}

data class Row(
    val key: String,
    val kind: Kind,
    val title: String = "",
    val subtitle: String = "",
    val release: Release? = null,
    val episode: Episode? = null,
    val selected: Int = 0,
    val click: (() -> Unit)? = null,
    val image: String? = null,
    val items: List<Release> = emptyList(),
    val cards: List<Row> = emptyList(),
    val watching: ContinueEpisode? = null,
    val longClick: (() -> Unit)? = null,
    val controls: List<Pair<String, () -> Unit>> = emptyList(),
    val composition: List<String>? = null,
    val focusLoad: (() -> Unit)? = null,
    val homeStyle: Boolean = false,
    val queryChanged: ((String) -> Unit)? = null,
    val searchDone: (() -> Unit)? = null,
    val favoriteTitle: String? = null,
    val episodeProgress: Pair<Long, Long>? = null,
    val profile: AccountProfile? = null,
    val activeControls: Set<Int> = emptySet(),
    val isFavorite: Boolean = false,
    val collectionType: CollectionType? = null,
)

class PageAdapter(
    private val tab: (Int) -> Unit,
    private val play: (Episode) -> Unit,
    private val favorite: (Release) -> Unit,
    private val details: (Release) -> Unit,
    private val progress: (String) -> Pair<Long, Long>,
    private val lastEpisode: (Int) -> String?,
    private val returnToTabs: () -> Unit,
    private val day: (Int) -> Unit = {},
    private val quickActions: (Release) -> Unit = {},
    private val shelfMode: Boolean = false,
    private val enterTab: (Int) -> Unit = tab,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var visibleShelfCards = 0f
    private val stableKeys = mutableMapOf<String, Long>()
    private var nextStableId = 0L

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long =
        stableKeys.getOrPut(rows[position].key) { nextStableId++ }

    fun rememberShelf(shelf: RecyclerView) = saveShelf(shelf)

    private fun View.cardDp(value: Int): Int {
        if (shelfMode && visibleShelfCards > 0) {
            val shelf = parent as? RecyclerView
            val viewport =
                ((shelf?.width?.takeIf { it > 0 }
                        ?: (resources.displayMetrics.widthPixels - dp(132))) -
                        (shelf?.paddingLeft ?: 0) -
                        (shelf?.paddingRight ?: 0))
                    .coerceAtLeast(1)
            val cardWidth = (viewport / visibleShelfCards - dp(8)).toInt()
            return (value * cardWidth / 144f).toInt()
        }
        return dp(
            if (shelfMode || getTag(R.id.shelf_key) == true) (value * 1.18f).toInt() else value
        )
    }

    private data class ShelfPosition(
        val focusedKey: String?,
        val anchorKey: String?,
        val offset: Int,
    )

    private val shelfPositions = mutableMapOf<String, ShelfPosition>()
    private val shelfResets = mutableMapOf<String, Boolean>()

    fun resetShelf(key: String, requestFocus: Boolean) {
        shelfResets[key] = requestFocus
    }

    private fun saveShelf(shelf: RecyclerView) {
        val key = shelf.getTag(R.id.shelf_key) as? String ?: return
        val child = shelf.adapter as? PageAdapter ?: return
        val manager =
            shelf.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager ?: return
        val first = manager.findFirstVisibleItemPosition()
        val focus = shelf.findFocus()?.let { shelf.findContainingItemView(it)?.tag as? String }
        shelfPositions[key] =
            ShelfPosition(
                focus ?: shelfPositions[key]?.focusedKey,
                child.rows.getOrNull(first)?.key,
                manager.findViewByPosition(first)?.left ?: 0,
            )
    }

    private var shelfFocusGeneration = 0

    fun cancelPendingShelfFocus() {
        shelfFocusGeneration++
    }

    fun restoreShelf(shelf: RecyclerView, requestFocus: Boolean = true) {
        val token = if (requestFocus) ++shelfFocusGeneration else shelfFocusGeneration
        val key = shelf.getTag(R.id.shelf_key) as? String ?: return
        val child = shelf.adapter as? PageAdapter ?: return
        val memory = shelfPositions[key]
        val index = child.rows.indexOfFirst { it.key == memory?.focusedKey }.takeIf { it >= 0 } ?: 0
        val manager = shelf.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
        if (requestFocus) {
            shelf.findViewHolderForAdapterPosition(index)?.itemView?.let {
                it.requestFocus()
                return
            }
        }
        if (
            !requestFocus &&
                (shelf.hasFocus() || shelf.scrollState != RecyclerView.SCROLL_STATE_IDLE)
        )
            return
        val anchor =
            child.rows.indexOfFirst { it.key == memory?.anchorKey }.takeIf { it >= 0 } ?: index
        manager.scrollToPositionWithOffset(anchor, memory?.offset ?: 0)
        if (requestFocus)
            shelf.post {
                if (
                    token != shelfFocusGeneration ||
                        shelf.getTag(R.id.shelf_key) != key ||
                        shelf.adapter !== child
                )
                    return@post
                val target = shelf.findViewHolderForAdapterPosition(index)?.itemView
                if (target != null) target.requestFocus()
                else {
                    manager.scrollToPosition(index)
                    shelf.post {
                        if (
                            token == shelfFocusGeneration &&
                                shelf.getTag(R.id.shelf_key) == key &&
                                shelf.adapter === child
                        )
                            shelf.findViewHolderForAdapterPosition(index)?.itemView?.requestFocus()
                    }
                }
            }
    }

    var rows: List<Row> = emptyList()
        private set

    private val attachedShelves = mutableSetOf<RecyclerView>()
    var tabStart: Int = -1
        private set

    var contentStart: Int = -1
        private set

    fun submit(value: List<Row>) {
        // Snapshot nested lists before the outer adapter is rebound.
        attachedShelves.forEach(::saveShelf)
        val old = rows
        val diff =
            androidx.recyclerview.widget.DiffUtil.calculateDiff(
                object : androidx.recyclerview.widget.DiffUtil.Callback() {
                    override fun getOldListSize() = old.size

                    override fun getNewListSize() = value.size

                    override fun areItemsTheSame(a: Int, b: Int) =
                        old[a].key == value[b].key && old[a].kind == value[b].kind

                    override fun areContentsTheSame(a: Int, b: Int) = old[a] == value[b]
                },
                false,
            )
        stableKeys.keys.retainAll(value.mapTo(HashSet()) { it.key })
        rows = value
        tabStart = rows.indexOfFirst { it.kind == Kind.TABS }
        contentStart = if (tabStart >= 0) tabStart + 1 else -1
        diff.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = rows.size

    override fun getItemViewType(position: Int) = rows[position].kind.ordinal

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val view: View =
            when (Kind.entries[type]) {
                Kind.CATALOG_BAR ->
                    object : HorizontalScrollView(context) {
                            override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
                                super.onSizeChanged(w, h, oldw, oldh)
                                getChildAt(0)?.let { line ->
                                    line.layoutParams =
                                        line.layoutParams.apply {
                                            width =
                                                (w - paddingLeft - paddingRight).coerceAtLeast(0)
                                        }
                                }
                                scrollTo(0, 0)
                            }
                        }
                        .apply {
                            isHorizontalScrollBarEnabled = false
                            isFillViewport = true
                            addView(
                                LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
                            )
                        }
                Kind.TABS,
                Kind.DAYS,
                Kind.ACCOUNT_TABS ->
                    HorizontalScrollView(context).apply {
                        isHorizontalScrollBarEnabled = false
                        addView(LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL })
                    }
                Kind.MORE,
                Kind.REFRESH ->
                    LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        setPadding(dp(12), dp(12), dp(12), dp(12))
                        addView(
                            ImageView(context).apply {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                isFocusable = false
                            },
                            LinearLayout.LayoutParams(dp(32), dp(32)),
                        )
                        addView(
                            TextView(context).apply {
                                gravity = Gravity.CENTER
                                maxLines = 2
                                includeFontPadding = false
                                isFocusable = false
                                ellipsize = android.text.TextUtils.TruncateAt.END
                            },
                            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) },
                        )
                    }
                Kind.SHELF ->
                    object : RecyclerView(context) {
                            override fun requestChildRectangleOnScreen(
                                child: View,
                                rect: android.graphics.Rect,
                                immediate: Boolean,
                            ): Boolean = super.requestChildRectangleOnScreen(child, rect, false)
                        }
                        .apply {
                            layoutManager =
                                androidx.recyclerview.widget
                                    .LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                                    .apply { isItemPrefetchEnabled = true }
                            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                            overScrollMode = View.OVER_SCROLL_NEVER
                            clipToPadding = false
                            itemAnimator = null
                            setPadding(dp(4), dp(4), dp(4), dp(4))
                        }
                Kind.SETTING,
                Kind.ACCOUNT_TILE ->
                    LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        addView(
                            ImageView(context).apply {
                                isFocusable = false
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            },
                            LinearLayout.LayoutParams(dp(28), dp(28)).apply { marginEnd = dp(18) },
                        )
                        addView(
                            LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                addView(TextView(context))
                                addView(
                                    TextView(context),
                                    LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) },
                                )
                            },
                            LinearLayout.LayoutParams(0, -2, 1f),
                        )
                        addView(
                            TextView(context).apply {
                                gravity = Gravity.END
                                maxLines = 1
                                ellipsize = android.text.TextUtils.TruncateAt.END
                            },
                            LinearLayout.LayoutParams(-2, -2).apply { marginStart = dp(20) },
                        )
                    }
                Kind.PROFILE ->
                    LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(0, dp(16), 0, dp(20))
                        addView(ImageView(context), LinearLayout.LayoutParams(dp(88), dp(88)))
                        addView(
                            TextView(context),
                            LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(20) },
                        )
                    }
                Kind.HERO -> LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                Kind.HEADING ->
                    LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, dp(8), 0, dp(4))
                        addView(TextView(context))
                        addView(
                            TextView(context),
                            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) },
                        )
                    }
                Kind.POSTER,
                Kind.FEATURE,
                Kind.GENRE,
                Kind.FRANCHISE,
                Kind.CONTINUE ->
                    object : FrameLayout(context) {
                            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                                val width = View.MeasureSpec.getSize(widthMeasureSpec)
                                if (Kind.entries[type] == Kind.FEATURE && width > 0) {
                                    val ratio = 16f / 9f
                                    super.onMeasure(
                                        widthMeasureSpec,
                                        View.MeasureSpec.makeMeasureSpec(
                                            (width / ratio).toInt(),
                                            View.MeasureSpec.EXACTLY,
                                        ),
                                    )
                                    return
                                }
                                if (
                                    Kind.entries[type] == Kind.FRANCHISE &&
                                        !shelfMode &&
                                        getTag(R.id.shelf_key) == true &&
                                        width > 0
                                ) {
                                    val imageWidth = (width * 150f / 312f).toInt()
                                    getChildAt(0)?.let { image ->
                                        if (image.layoutParams.width != imageWidth)
                                            image.layoutParams =
                                                image.layoutParams.apply { this.width = imageWidth }
                                    }
                                    getChildAt(2)?.let { content ->
                                        val params =
                                            content.layoutParams as FrameLayout.LayoutParams
                                        if (params.marginStart != imageWidth)
                                            content.layoutParams =
                                                params.apply { marginStart = imageWidth }
                                    }
                                    findViewWithTag<LinearLayout>("franchise-composition")?.let {
                                        overlay ->
                                        if (overlay.layoutParams.width != imageWidth)
                                            overlay.layoutParams =
                                                overlay.layoutParams.apply {
                                                    this.width = imageWidth
                                                }
                                    }
                                    super.onMeasure(
                                        widthMeasureSpec,
                                        View.MeasureSpec.makeMeasureSpec(
                                            (width * 190f / 312f).toInt(),
                                            View.MeasureSpec.EXACTLY,
                                        ),
                                    )
                                } else super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                            }
                        }
                        .apply {
                            isFocusable = true
                            isClickable = true
                            focusBackground(radiusDp = 12)
                            clipToOutline = true
                            setPadding(dp(3), dp(3), dp(3), dp(3))
                            addView(
                                ImageView(context).apply {
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                },
                                FrameLayout.LayoutParams(-1, -1),
                            )
                            addView(
                                View(context).apply {
                                    background =
                                        GradientDrawable(
                                            GradientDrawable.Orientation.BOTTOM_TOP,
                                            intArrayOf(0xF5121212.toInt(), 0x10121212),
                                        )
                                },
                                FrameLayout.LayoutParams(-1, -1),
                            )
                            addView(
                                LinearLayout(context).apply {
                                    orientation = LinearLayout.VERTICAL
                                    setPadding(dp(12), dp(12), dp(12), dp(12))
                                    addView(
                                        TextView(context).apply {
                                            maxLines = 2
                                            ellipsize = android.text.TextUtils.TruncateAt.END
                                        }
                                    )
                                    addView(
                                        TextView(context).apply {
                                            maxLines = 4
                                            ellipsize = android.text.TextUtils.TruncateAt.END
                                        },
                                        LinearLayout.LayoutParams(-1, -2).apply {
                                            topMargin = dp(4)
                                        },
                                    )
                                },
                                FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM),
                            )
                            if (Kind.entries[type] == Kind.CONTINUE)
                                addView(
                                    android.widget
                                        .ProgressBar(
                                            context,
                                            null,
                                            android.R.attr.progressBarStyleHorizontal,
                                        )
                                        .apply {
                                            max = 1000
                                            progressTintList = ColorStateList.valueOf(RED)
                                        },
                                    FrameLayout.LayoutParams(-1, dp(4), Gravity.TOP).apply {
                                        topMargin = cardDp(150) - dp(4)
                                    },
                                )
                        }
                Kind.EPISODE ->
                    FrameLayout(context).apply {
                        isFocusable = true
                        isClickable = true
                        focusBackground()
                        setPadding(dp(3), dp(3), dp(3), dp(3))
                        addView(
                            ImageView(context).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            },
                            FrameLayout.LayoutParams(-1, -1),
                        )
                        addView(
                            View(context).apply {
                                background =
                                    GradientDrawable(
                                        GradientDrawable.Orientation.BOTTOM_TOP,
                                        intArrayOf(0xF0101010.toInt(), 0x18101010),
                                    )
                            },
                            FrameLayout.LayoutParams(-1, -1),
                        )
                        addView(
                            TextView(context).apply {
                                setPadding(dp(12), dp(8), dp(12), dp(12))
                                maxLines = 3
                            },
                            FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM),
                        )
                        addView(
                            android.widget
                                .ProgressBar(
                                    context,
                                    null,
                                    android.R.attr.progressBarStyleHorizontal,
                                )
                                .apply {
                                    max = 1000
                                    progressTintList = ColorStateList.valueOf(RED)
                                },
                            FrameLayout.LayoutParams(-1, dp(3), Gravity.BOTTOM),
                        )
                    }
                else -> TextView(context)
            }
        view.layoutParams =
            RecyclerView.LayoutParams(
                    if (shelfMode)
                        view.cardDp(
                            when (Kind.entries[type]) {
                                Kind.CONTINUE -> 272
                                Kind.FEATURE -> 232
                                Kind.GENRE -> 172
                                Kind.FRANCHISE -> 312
                                else -> 144
                            }
                        )
                    else -1,
                    when (Kind.entries[type]) {
                        Kind.CONTINUE -> view.cardDp(242)
                        Kind.EPISODE -> view.dp(160)
                        Kind.GENRE -> view.cardDp(236)
                        Kind.FRANCHISE -> view.cardDp(190)
                        Kind.POSTER -> view.cardDp(270)
                        Kind.MORE -> view.cardDp(270)
                        Kind.SHELF -> view.dp(286)
                        Kind.FEATURE -> view.cardDp(204)
                        else -> -2
                    },
                )
                .apply {
                    val card = Kind.entries[type] in listOf(Kind.POSTER, Kind.EPISODE, Kind.FEATURE)
                    setMargins(view.dp(4), view.dp(4), view.dp(4), view.dp(4))
                }
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = rows[position]
        val view = holder.itemView
        val inContent = contentStart >= 0 && position >= contentStart
        view.tag = row.key
        if (shelfMode && visibleShelfCards > 0)
            view.layoutParams = view.layoutParams.apply { width = view.cardDp(144) }
        if (row.kind != Kind.SHELF) view.setTag(R.id.shelf_key, row.homeStyle)
        val firstContent =
            if (contentStart >= 0)
                (contentStart until rows.size).firstOrNull { rows[it].kind != Kind.HEADING }
            else null
        val firstRowCount =
            when (firstContent?.let { rows[it].kind }) {
                Kind.POSTER -> 5
                Kind.EPISODE -> 3
                else -> 1
            }
        view.setOnKeyListener { _, key, event ->
            if (
                firstContent != null &&
                    holder.bindingAdapterPosition in
                        firstContent until (firstContent + firstRowCount) &&
                    key == KeyEvent.KEYCODE_DPAD_UP
            ) {
                if (event.action == KeyEvent.ACTION_UP) returnToTabs()
                true
            } else false
        }
        when (row.kind) {
            Kind.PROFILE -> {
                val box = view as LinearLayout
                val profile = row.profile
                (box.getChildAt(0) as ImageView).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = shape(0xFF292929.toInt(), dp(44).toFloat())
                    clipToOutline = true
                    load(profile?.avatar ?: R.drawable.ic_aniliberty)
                    isFocusable = false
                    contentDescription = profile?.name ?: "Гость"
                }
                (box.getChildAt(1) as TextView).apply {
                    label(
                        listOf(
                                profile?.name ?: "Гость",
                                profile?.login?.takeIf { it != profile?.name }.orEmpty(),
                                profile?.email.orEmpty(),
                                profile
                                    ?.createdAt
                                    ?.let {
                                        updateTimestamp(it)
                                            .substringBefore(" ")
                                            .takeIf { date -> date.isNotBlank() }
                                            ?.let { date -> "В AniLiberty с $date" }
                                    }
                                    .orEmpty(),
                            )
                            .filter { it.isNotBlank() }
                            .joinToString("\n"),
                        18f,
                        WHITE,
                        true,
                    )
                    isFocusable = false
                }
            }
            Kind.SHELF -> {
                val shelf = view as RecyclerView
                val oldKey = shelf.getTag(R.id.shelf_key) as? String
                if (oldKey != row.key) {
                    shelf.adapter =
                        PageAdapter(
                            tab,
                            play,
                            favorite,
                            details,
                            progress,
                            lastEpisode,
                            returnToTabs,
                            day,
                            quickActions,
                            true,
                            enterTab,
                        )
                    shelf.scrollToPosition(0)
                }
                shelf.setTag(R.id.shelf_key, row.key)
                shelf.layoutParams =
                    shelf.layoutParams.apply {
                        height =
                            view.dp(
                                (when (row.selected) {
                                        1 -> 220
                                        2 -> 252
                                        3 -> 206
                                        4 -> 222
                                        5 -> 166
                                        else -> 286
                                    } * 1.18f)
                                    .toInt()
                            )
                    }
                val cards =
                    if (row.cards.isNotEmpty()) row.cards
                    else
                        row.items.map {
                            Row("${row.key}:release:${it.id}", Kind.POSTER, release = it)
                        }
                val nested = shelf.adapter as PageAdapter
                nested.visibleShelfCards = if (row.selected in listOf(6, 7)) 4.5f else 0f
                if (row.selected in listOf(6, 7)) {
                    val viewport =
                        (shelf.width.takeIf { it > 0 }
                            ?: (view.resources.displayMetrics.widthPixels - view.dp(132))) -
                            shelf.paddingLeft -
                            shelf.paddingRight
                    val cardWidth = (viewport / 4.5f - view.dp(8)).toInt()
                    shelf.layoutParams =
                        shelf.layoutParams.apply {
                            height = (cardWidth * 206f / 144f).toInt() + view.dp(24)
                        }
                }
                if (row.selected == 1 && cards.all { it.kind == Kind.FEATURE })
                    shelf.layoutParams =
                        shelf.layoutParams.apply {
                            height = view.dp((232 * 1.18f * 9 / 16).toInt() + 20)
                        }
                nested.submit(cards)
                if (shelfResets.containsKey(row.key)) {
                    val focusFirst = shelfResets.remove(row.key) == true
                    shelfPositions.remove(row.key)
                    (shelf.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
                        .scrollToPositionWithOffset(0, shelf.paddingLeft)
                    if (focusFirst)
                        shelf.post {
                            if (
                                shelf.isAttachedToWindow &&
                                    (shelf.hasFocus() || shelf.rootView.findFocus() == null)
                            )
                                shelf.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                        }
                } else restoreShelf(shelf, false)
            }
            Kind.MORE,
            Kind.REFRESH -> {
                val box = view as LinearLayout
                box.layoutParams =
                    box.layoutParams.apply {
                        width = view.dp(136)
                        height =
                            view.cardDp(
                                when (row.selected) {
                                    1 -> 204
                                    2 -> 236
                                    3 -> 190
                                    4 -> 206
                                    5 -> 150
                                    else -> 270
                                }
                            )
                        if (this is RecyclerView.LayoutParams) topMargin = view.dp(4)
                    }
                val icon = box.getChildAt(0) as ImageView
                icon.layoutParams = LinearLayout.LayoutParams(view.dp(32), view.dp(32))
                icon.setImageResource(
                    if (row.kind == Kind.REFRESH) R.drawable.ic_refresh
                    else R.drawable.ic_arrow_right
                )
                icon.setColorFilter(WHITE)
                val caption = box.getChildAt(1) as TextView
                caption.label(
                    if (row.kind == Kind.REFRESH) row.title else "Открыть всё",
                    14f,
                    WHITE,
                    true,
                )
                caption.gravity = Gravity.CENTER
                caption.setSingleLine(true)
                box.gravity = Gravity.CENTER
                box.isFocusable = true
                box.isClickable = row.click != null
                box.webControlBackground()
                box.contentDescription =
                    if (row.kind == Kind.REFRESH) row.title
                    else "Открыть раздел ${row.title} полностью"
                box.setOnClickListener(
                    row.click?.let { callback -> View.OnClickListener { callback() } }
                )
                box.isClickable = row.click != null
            }
            Kind.CATALOG_BAR -> {
                fun decorate(button: View, index: Int) {
                    button.isSelected = index in row.activeControls
                    button.webControlBackground(button.isSelected)
                    if (button is TextView)
                        button.compoundDrawablesRelative.filterNotNull().forEach {
                            it.setTintList(
                                if (button.isSelected) ColorStateList.valueOf(RED)
                                else button.textColors
                            )
                        }
                    button.contentDescription =
                        row.controls[index].first + if (button.isSelected) " · включено" else ""
                }
                fun controlIcon(index: Int): Int =
                    when (row.controls[index].first) {
                        "Избранное" -> R.drawable.ic_favorite
                        "Коллекции" -> R.drawable.ic_library
                        "Сбросить" -> R.drawable.ic_refresh
                        else -> if (index == 1) R.drawable.ic_filter else R.drawable.ic_sort
                    }
                val line = (view as HorizontalScrollView).getChildAt(0) as LinearLayout
                view.layoutParams = view.layoutParams.apply { height = view.dp(60) }
                if (row.selected == 1) {
                    val existing = line.getChildAt(0) as? android.widget.EditText
                    if (existing != null && line.childCount == row.controls.size) {
                        row.controls.drop(1).forEachIndexed { index, control ->
                            line.getChildAt(index + 1).apply {
                                decorate(this, index + 1)
                                setOnClickListener { control.second() }
                            }
                        }
                        return
                    }
                    line.removeAllViews()
                    line.setPadding(0, view.dp(4), 0, view.dp(8))
                    val input =
                        object : android.widget.EditText(view.context) {
                                override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
                                    if (
                                        keyCode == KeyEvent.KEYCODE_BACK &&
                                            event.action == KeyEvent.ACTION_UP
                                    ) {
                                        row.searchDone?.invoke()
                                        return true
                                    }
                                    return super.onKeyPreIme(keyCode, event)
                                }
                            }
                            .apply {
                                setText(row.title)
                                hint = "Поиск по каталогу…"
                                setSingleLine()
                                textSize = 18f
                                setTextColor(WHITE)
                                setHintTextColor(MUTED)
                                setPadding(dp(16), 0, dp(16), 0)
                                webControlBackground()
                                imeOptions =
                                    android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH or
                                        android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI
                                inputType = android.text.InputType.TYPE_CLASS_TEXT
                                addTextChangedListener(
                                    object : android.text.TextWatcher {
                                        override fun beforeTextChanged(
                                            s: CharSequence?,
                                            start: Int,
                                            count: Int,
                                            after: Int,
                                        ) {}

                                        override fun onTextChanged(
                                            s: CharSequence?,
                                            start: Int,
                                            before: Int,
                                            count: Int,
                                        ) {
                                            row.queryChanged?.invoke(s.toString())
                                        }

                                        override fun afterTextChanged(s: android.text.Editable?) {}
                                    }
                                )
                                setOnEditorActionListener { _, action, _ ->
                                    if (
                                        action ==
                                            android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
                                    ) {
                                        row.searchDone?.invoke()
                                        true
                                    } else false
                                }
                                setOnKeyListener { _, key, event ->
                                    if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
                                        if (event.action == KeyEvent.ACTION_DOWN)
                                            row.searchDone?.invoke()
                                        true
                                    } else false
                                }
                            }
                    line.addView(
                        input,
                        LinearLayout.LayoutParams(0, view.dp(48), 1f).apply {
                            marginEnd = view.dp(8)
                        },
                    )
                    row.controls.drop(1).forEachIndexed { index, (title, callback) ->
                        val icon = controlIcon(index + 1)
                        line.addView(
                            action(line, "", icon = icon, click = callback).apply {
                                decorate(this, index + 1)
                                setPadding(dp(14), 0, dp(14), 0)
                                compoundDrawablePadding = 0
                            },
                            LinearLayout.LayoutParams(view.dp(48), view.dp(48)).apply {
                                if (index < row.controls.size - 2) marginEnd = view.dp(8)
                            },
                        )
                    }
                    view.scrollTo(0, 0)
                    input.requestFocus()
                    input.setSelection(input.text.length)
                    input.post {
                        (view.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                                as android.view.inputmethod.InputMethodManager)
                            .showSoftInput(
                                input,
                                android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT,
                            )
                    }
                    return
                }
                if (
                    line.childCount == row.controls.size &&
                        row.controls.indices.all {
                            (line.getChildAt(it) as? TextView)?.text?.toString() ==
                                row.controls[it].first
                        }
                ) {
                    row.controls.forEachIndexed { index, control ->
                        line.getChildAt(index).apply {
                            decorate(this, index)
                            setOnClickListener { control.second() }
                        }
                    }
                    return
                }
                val focused = line.indexOfChild(line.findFocus())
                line.removeAllViews()
                line.setPadding(0, view.dp(4), 0, view.dp(8))
                row.controls.forEachIndexed { index, (title, callback) ->
                    val icon = if (index == 0) R.drawable.ic_search else controlIcon(index)
                    line.addView(
                        action(line, title, icon = icon, click = callback).apply {
                            textSize = 14f
                            setSingleLine()
                            ellipsize = android.text.TextUtils.TruncateAt.END
                            if (index == 0) gravity = Gravity.CENTER_VERTICAL or Gravity.START
                            decorate(this, index)
                        },
                        LinearLayout.LayoutParams(
                                if (index == 0) 0 else -2,
                                view.dp(48),
                                if (index == 0) 1f else 0f,
                            )
                            .apply { if (index < row.controls.lastIndex) marginEnd = view.dp(8) },
                    )
                }
                view.scrollTo(0, 0)
                if (focused >= 0) line.getChildAt(focused)?.requestFocus()
            }
            Kind.DAYS -> {
                val line = (view as HorizontalScrollView).getChildAt(0) as LinearLayout
                val offset = if (row.title.isNotBlank()) 1 else 0
                if (line.childCount != 3 + offset) {
                    line.removeAllViews()
                    if (offset == 1)
                        line.addView(
                            TextView(view.context).apply {
                                label(row.title, 24f, WHITE, true)
                                isFocusable = false
                            },
                            LinearLayout.LayoutParams(-2, -2).apply { marginEnd = view.dp(24) },
                        )
                    listOf("Вчера", "Сегодня", "Завтра").forEachIndexed { index, title ->
                        line.addView(
                            action(line, title, click = { day(index) }).apply {
                                tag = "schedule-tab:$index"
                                textSize = 16f
                                webControlBackground()
                            },
                            LinearLayout.LayoutParams(-2, view.dp(48)).apply {
                                marginEnd = view.dp(8)
                            },
                        )
                    }
                }
                (0..2).forEach { index ->
                    (line.getChildAt(index + offset) as TextView).apply {
                        setTextColor(if (index == row.selected) RED else WHITE)
                        setOnClickListener { day(index) }
                    }
                }
            }
            Kind.ACCOUNT_TABS -> {
                val line = (view as HorizontalScrollView).getChildAt(0) as LinearLayout
                if (line.childCount != row.controls.size) {
                    line.removeAllViews()
                    row.controls.forEachIndexed { index, (title, callback) ->
                        val collectionType =
                            if (row.key == "account-collections-tabs") CollectionType.entries.getOrNull(index)
                            else null
                        line.addView(
                            action(line, title, icon = collectionType?.icon ?: 0, click = callback),
                            LinearLayout.LayoutParams(-2, view.dp(44)).apply {
                                marginEnd = view.dp(8)
                            },
                        )
                    }
                }
                row.controls.forEachIndexed { i, (title, callback) ->
                    (line.getChildAt(i) as TextView).apply {
                        text = title
                        val collectionType =
                            if (row.key == "account-collections-tabs") CollectionType.entries.getOrNull(i)
                            else null
                        setTextColor(if (i == row.selected) collectionType?.color ?: RED else WHITE)
                        compoundDrawablesRelative[0]?.mutate()?.setTint(collectionType?.color ?: WHITE)
                        setOnClickListener { callback() }
                    }
                }
            }
            Kind.TABS -> {
                view.isFocusable = false
                val line = (view as HorizontalScrollView).getChildAt(0) as LinearLayout
                val labels =
                    listOf(
                        "Серии${row.title.takeIf{it.isNotBlank()}?.let{" · $it"}.orEmpty()}",
                        "Связанные",
                        "Похожее",
                        "Команда",
                    )
                val icons =
                    listOf(
                        R.drawable.ic_episodes,
                        R.drawable.ic_related,
                        R.drawable.ic_similar,
                        R.drawable.ic_team,
                    )
                if (line.childCount != labels.size) {
                    line.removeAllViews()
                    labels.forEachIndexed { i, label ->
                        val button =
                            action(line, label, icon = icons[i], click = { tab(i) }).apply {
                                id =
                                    listOf(
                                        R.id.detail_tab_0,
                                        R.id.detail_tab_1,
                                        R.id.detail_tab_2,
                                        R.id.detail_tab_3,
                                    )[i]
                                tag = "detail-tab:$i"
                                textSize = 15f
                                setOnKeyListener { _, key, event ->
                                    if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
                                        if (event.action == KeyEvent.ACTION_UP) enterTab(i)
                                        true
                                    } else false
                                }
                            }
                        line.addView(
                            LinearLayout(view.context).apply {
                                orientation = LinearLayout.VERTICAL
                                addView(button, LinearLayout.LayoutParams(-2, view.dp(42)))
                                addView(
                                    View(view.context),
                                    LinearLayout.LayoutParams(-1, view.dp(2)),
                                )
                            },
                            LinearLayout.LayoutParams(-2, -2).apply { marginEnd = view.dp(8) },
                        )
                    }
                }
                labels.forEachIndexed { i, label ->
                    val wrapper = line.getChildAt(i) as LinearLayout
                    val button = wrapper.getChildAt(0) as TextView
                    button.text = label
                    button.setTextColor(
                        if (button.hasFocus()) WHITE else if (i == row.selected) RED else MUTED
                    )
                    button.setOnFocusChangeListener { _, focused ->
                        button.setTextColor(
                            if (focused) WHITE else if (i == row.selected) RED else MUTED
                        )
                    }
                    wrapper
                        .getChildAt(1)
                        .setBackgroundColor(if (i == row.selected) RED else Color.TRANSPARENT)
                }
            }
            Kind.HERO -> {
                val r = row.release!!
                val box = view as LinearLayout
                val focusedAction =
                    (box.findFocus()?.tag as? String)?.takeIf {
                        box.getTag(R.id.hero_release) == r.id
                    }
                val previousControls =
                    box.findViewWithTag<LinearLayout>("hero-controls")?.takeIf {
                        box.getTag(R.id.hero_release) == r.id
                    }
                val previousButtons =
                    previousControls
                        ?.let { line ->
                            (0 until line.childCount).map { line.getChildAt(it) as TextView }
                        }
                        .orEmpty()
                val previousScroll =
                    (previousControls?.parent as? HorizontalScrollView)?.scrollX ?: 0
                // Detach action views before rebuilding metadata: the same button retains its focus
                // identity.
                previousControls?.removeAllViews()
                box.setTag(R.id.hero_release, r.id)
                box.removeAllViews()
                box.setPadding(0, view.dp(12), 0, view.dp(16))
                box.gravity = Gravity.TOP
                box.addView(
                    ImageView(view.context).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        load(r.poster)
                        background = shape(0xFF212121.toInt(), dp(8).toFloat())
                        clipToOutline = true
                        contentDescription = r.title
                        isFocusable = false
                    },
                    LinearLayout.LayoutParams(view.dp(190), view.dp(272)),
                )
                val info =
                    LinearLayout(view.context).apply {
                        orientation = LinearLayout.VERTICAL
                        minimumHeight = dp(272)
                        setPadding(dp(24), 0, 0, 0)
                    }
                box.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
                fun text(
                    value: String,
                    size: Float,
                    color: Int = WHITE,
                    bold: Boolean = false,
                    lines: Int = 2,
                ) {
                    if (value.isBlank()) return
                    info.addView(
                        TextView(view.context).apply {
                            label(value, size, color, bold)
                            maxLines = lines
                            ellipsize = android.text.TextUtils.TruncateAt.END
                            includeFontPadding = false
                        },
                        LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = view.dp(7) },
                    )
                }
                text(r.title, 27f, WHITE, true)
                text(r.english, 13f, MUTED, false, 1)
                val ratings = LinearLayout(view.context).apply { gravity = Gravity.CENTER_VERTICAL }
                info.addView(
                    HorizontalScrollView(view.context).apply {
                        isFocusable = false
                        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        isHorizontalScrollBarEnabled = false
                        addView(ratings)
                    },
                    LinearLayout.LayoutParams(-1, view.dp(36)).apply { bottomMargin = view.dp(8) },
                )
                r.ratings.forEach { rating ->
                    val badge =
                        LinearLayout(view.context).apply {
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dp(8), dp(3), dp(8), dp(3))
                            background = shape(0xFF212121.toInt(), dp(6).toFloat())
                            contentDescription = "${rating.source}: ${rating.value}"
                        }
                    val file = if (rating.source == "Shikimori") "shikimori" else "myanimelist"
                    badge.addView(
                        ImageView(view.context).apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            isFocusable = false
                            contentDescription = rating.source
                            load("file:///android_asset/brands/$file.svg") {
                                decoderFactory(coil3.svg.SvgDecoder.Factory())
                            }
                        },
                        LinearLayout.LayoutParams(
                            view.dp(if (file == "shikimori") 24 else 36),
                            view.dp(24),
                        ),
                    )
                    badge.addView(
                        TextView(view.context).apply {
                            label("%.1f".format(rating.value), 16f, WHITE, true)
                        },
                        LinearLayout.LayoutParams(-2, -2).apply { marginStart = view.dp(7) },
                    )
                    ratings.addView(
                        badge,
                        LinearLayout.LayoutParams(-2, view.dp(32)).apply { marginEnd = view.dp(8) },
                    )
                }
                r.favoriteCount?.let { count ->
                    ratings.addView(
                        TextView(view.context).apply {
                            label(compactCount(count), 16f, WHITE, true)
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dp(8), 0, dp(8), 0)
                            compoundDrawablePadding = dp(7)
                            setCompoundDrawablesRelative(
                                context.getDrawable(R.drawable.ic_favorite)?.apply {
                                    setBounds(0, 0, dp(24), dp(24))
                                },
                                null,
                                null,
                                null,
                            )
                            background = shape(0xFF212121.toInt(), dp(6).toFloat())
                            contentDescription = "В избранном у $count пользователей"
                        },
                        LinearLayout.LayoutParams(-2, view.dp(32)).apply { marginEnd = view.dp(8) },
                    )
                }
                r.ongoing?.let { ongoing ->
                    val color = if (ongoing) 0xFF68B5FF.toInt() else 0xFF78C99F.toInt()
                    ratings.addView(
                        TextView(view.context).apply {
                            label(if (ongoing) "Онгоинг" else "Завершён", 14f, color, true)
                            gravity = Gravity.CENTER
                            setPadding(dp(10), 0, dp(10), 0)
                            background =
                                shape(if (ongoing) 0x1A68B5FF else 0x1A78C99F, dp(6).toFloat())
                        },
                        LinearLayout.LayoutParams(-2, view.dp(32)),
                    )
                }
                if (r.ratings.isEmpty() && r.ongoing == null && r.favoriteCount == null)
                    ratings.parent.let { (it as View).visibility = View.GONE }
                val episode = resumeEpisode(r.episodes, lastEpisode(r.id), TvSettings(view.context).watchedPercent, progress)
                info.addView(
                    View(view.context).apply { tag = "title-bottom-spacer" },
                    LinearLayout.LayoutParams(-1, 0, 1f),
                )
                updateTimestamp(r.updatedAt)
                    .takeIf { it.isNotBlank() }
                    ?.let { text("Обновлено $it", 12f, MUTED, false, 1) }
                val age = Regex("[0-9]+").find(r.age)?.value.orEmpty()
                text(
                    listOf(r.year, r.season, r.type, age)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    14f,
                    MUTED,
                    false,
                    1,
                )
                text(r.genres.joinToString(" · "), 14f, WHITE, false, 2)
                val controls =
                    LinearLayout(view.context).apply {
                        tag = "hero-controls"
                        gravity = Gravity.BOTTOM
                    }
                info.addView(
                    HorizontalScrollView(view.context).apply {
                        isFocusable = false
                        isHorizontalScrollBarEnabled = false
                        addView(controls)
                    },
                    LinearLayout.LayoutParams(-1, view.dp(48)).apply { topMargin = view.dp(4) },
                )
                fun button(
                    label: String,
                    icon: Int,
                    primary: Boolean = false,
                    description: String = label,
                    click: () -> Unit,
                ) {
                    val favorite = icon == R.drawable.ic_favorite
                    val key = "hero-action:$icon"
                    val button =
                        previousButtons.firstOrNull { it.tag == key }
                            ?: action(controls, if (favorite) "" else label, primary, icon, click)
                    button.apply {
                        id =
                            when (icon) {
                                R.drawable.ic_play -> R.id.hero_play
                                R.drawable.ic_favorite -> R.id.hero_favorite
                                R.drawable.ic_library -> R.id.hero_collection
                                R.drawable.ic_comments -> R.id.hero_description
                                else -> R.id.hero_more
                            }
                        tag = key
                        text = if (favorite) "" else label
                        textSize = 14f
                        contentDescription = description
                        setOnClickListener { click() }
                        CollectionType.entries.firstOrNull { it.title == label }?.let { type ->
                            compoundDrawablesRelative[0]?.mutate()?.setTint(type.color)
                        }
                        if (favorite) {
                            gravity = Gravity.CENTER
                            compoundDrawablePadding = 0
                            setPadding(dp(11), 0, dp(11), 0)
                            setTextColor(if (row.selected == 1) 0xFFFFD166.toInt() else WHITE)
                            compoundDrawablesRelative[0]?.apply {
                                setBounds(0, 0, dp(24), dp(24))
                                setTint(if (row.selected == 1) 0xFFFFD166.toInt() else WHITE)
                            }
                        }
                    }
                    controls.addView(
                        button,
                        LinearLayout.LayoutParams(if (favorite) view.dp(46) else -2, view.dp(46))
                            .apply { marginEnd = view.dp(8) },
                    )
                }
                if (episode != null && episode.streams.isNotEmpty() && !r.blocked) {
                    val following =
                        r.episodes.any { it.id == lastEpisode(r.id) } &&
                            episode.id != lastEpisode(r.id)
                    val label =
                        if (following) "Следующая серия"
                        else if (progress(episode.id).first > 0) "Продолжить" else "Смотреть"
                    button(label, R.drawable.ic_play, true, "$label · ${episode.label}") {
                        play(episode)
                    }
                }
                button(
                    row.favoriteTitle ?: if (row.selected == 1) "В библиотеке" else "В библиотеку",
                    R.drawable.ic_favorite,
                    description =
                        row.favoriteTitle
                            ?: if (row.selected == 1) "Убрать из библиотеки"
                            else "Добавить в библиотеку",
                ) {
                    favorite(r)
                }
                row.controls.forEach { (label, callback) ->
                    button(
                        label,
                        when {
                            label == "Описание" -> R.drawable.ic_comments
                            CollectionType.entries.any { it.title == label } ->
                                CollectionType.entries.first { it.title == label }.icon
                            label == "Коллекция" -> R.drawable.ic_library
                            else -> R.drawable.ic_settings
                        },
                        click = callback,
                    )
                }
                (controls.parent as HorizontalScrollView).scrollTo(previousScroll, 0)
                if (focusedAction != null) box.findViewWithTag<View>(focusedAction)?.requestFocus()
            }
            Kind.GENRE,
            Kind.FRANCHISE -> {
                val box = view as FrameLayout
                val image = box.getChildAt(0) as ImageView
                val shade = box.getChildAt(1)
                val content = box.getChildAt(2) as LinearLayout
                val title = content.getChildAt(0) as TextView
                val meta = content.getChildAt(1) as TextView
                val genre = row.kind == Kind.GENRE
                val cardStyleHome = shelfMode || row.homeStyle
                val franchisePage = row.kind == Kind.FRANCHISE && !cardStyleHome
                if (row.homeStyle)
                    box.layoutParams =
                        box.layoutParams.apply { height = view.cardDp(if (genre) 236 else 190) }
                if (franchisePage)
                    box.layoutParams = box.layoutParams.apply { height = view.dp(180) }
                box.focusBackground(radiusDp = 12)
                box.clipToOutline = true
                image.load(row.image)
                image.scaleType = ImageView.ScaleType.CENTER_CROP
                if (genre || franchisePage) {
                    image.layoutParams = FrameLayout.LayoutParams(-1, -1)
                    shade.background = GenreShade()
                    shade.visibility = View.VISIBLE
                    content.layoutParams = FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM)
                    content.setPadding(view.dp(12), view.dp(12), view.dp(12), view.dp(18))
                    title.label(row.title, if (cardStyleHome) 20f else 17f, WHITE, true)
                    title.gravity = if (franchisePage) Gravity.START else Gravity.CENTER
                    title.maxLines = 2
                    meta.label(
                        row.subtitle,
                        if (cardStyleHome) 14f else 12f,
                        0xFFBDBDBD.toInt(),
                        true,
                    )
                    meta.gravity = if (franchisePage) Gravity.START else Gravity.CENTER
                    meta.maxLines = 1
                    title.layoutParams = LinearLayout.LayoutParams(-1, -2)
                    meta.layoutParams =
                        LinearLayout.LayoutParams(-1, -2).apply { topMargin = view.dp(5) }
                    content.setBackgroundColor(Color.TRANSPARENT)
                } else {
                    val width =
                        if (cardStyleHome) view.cardDp(150)
                        else ((view.resources.displayMetrics.widthPixels - view.dp(140)) / 3 / 2)
                    image.layoutParams = FrameLayout.LayoutParams(width, -1, Gravity.START)
                    shade.visibility = View.GONE
                    content.layoutParams =
                        FrameLayout.LayoutParams(-1, -1).apply { marginStart = width }
                    content.setPadding(view.dp(14), view.dp(14), view.dp(12), view.dp(14))
                    content.setBackgroundColor(0xFF212121.toInt())
                    title.label(row.title, if (cardStyleHome) 18f else 15f, WHITE, true)
                    title.gravity = Gravity.START
                    title.maxLines = 4
                    meta.label(row.subtitle, if (cardStyleHome) 14f else 12f, MUTED)
                    meta.gravity = Gravity.BOTTOM
                    meta.maxLines = 4
                    title.layoutParams = LinearLayout.LayoutParams(-1, -2)
                    meta.layoutParams =
                        LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = view.dp(10) }
                }
                meta.visibility = if (row.subtitle.isBlank()) View.GONE else View.VISIBLE
                val baseImage =
                    FrameLayout.LayoutParams(image.layoutParams as FrameLayout.LayoutParams)
                val baseContent =
                    FrameLayout.LayoutParams(content.layoutParams as FrameLayout.LayoutParams)
                val composition =
                    box.findViewWithTag<LinearLayout>("franchise-composition")
                        ?: LinearLayout(view.context).apply {
                            tag = "franchise-composition"
                            orientation = LinearLayout.VERTICAL
                            gravity = Gravity.CENTER_VERTICAL
                            isFocusable = false
                            setPadding(view.dp(8), view.dp(8), view.dp(8), view.dp(8))
                            box.addView(
                                this,
                                FrameLayout.LayoutParams(baseImage.width, -1, Gravity.START),
                            )
                        }
                composition.layoutParams =
                    FrameLayout.LayoutParams(baseImage.width, -1, Gravity.START)
                composition.removeAllViews()
                if (!genre) {
                    val names =
                        if (!row.composition.isNullOrEmpty())
                            row.composition.mapIndexed { index, name -> "${index+1}. $name" }
                        else
                            listOf(
                                if (row.selected == -1) "Состав недоступен"
                                else if (row.composition != null) "Состав пока не указан"
                                else "Загрузка состава…"
                            )
                    names.forEach { name ->
                        composition.addView(
                            TextView(view.context).apply {
                                label(
                                    name,
                                    if ((row.composition?.size ?: 0) > 9) 11f else 13f,
                                    WHITE,
                                )
                                setSingleLine()
                                ellipsize = android.text.TextUtils.TruncateAt.END
                                isFocusable = false
                                includeFontPadding = false
                            },
                            LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = view.dp(3) },
                        )
                    }
                }
                fun featureFocus(focused: Boolean) {
                    image.colorFilter =
                        if (focused)
                            android.graphics.ColorMatrixColorFilter(
                                floatArrayOf(
                                    .15f,
                                    0f,
                                    0f,
                                    0f,
                                    0f,
                                    0f,
                                    .15f,
                                    0f,
                                    0f,
                                    0f,
                                    0f,
                                    0f,
                                    .15f,
                                    0f,
                                    0f,
                                    0f,
                                    0f,
                                    0f,
                                    1f,
                                    0f,
                                )
                            )
                        else null
                    composition.visibility = if (focused && !genre) View.VISIBLE else View.GONE
                    image.layoutParams = FrameLayout.LayoutParams(baseImage)
                    content.layoutParams = FrameLayout.LayoutParams(baseContent)
                    title.visibility = View.VISIBLE
                    meta.visibility = if (row.subtitle.isBlank()) View.GONE else View.VISIBLE
                }
                box.contentDescription =
                    listOf(row.title, row.subtitle).filter(String::isNotBlank).joinToString(". ")
                box.setOnClickListener { row.click?.invoke() }
                featureFocus(box.hasFocus())
                box.setOnFocusChangeListener { _, focused -> featureFocus(focused) }
                if (!genre && row.composition == null)
                    view.post { if (view.tag == row.key) row.focusLoad?.invoke() }
            }
            Kind.FEATURE,
            Kind.POSTER,
            Kind.CONTINUE -> {
                val homeCard = shelfMode || row.homeStyle
                val box = view as FrameLayout
                val content = box.getChildAt(2) as LinearLayout
                val title = content.getChildAt(0) as TextView
                val subtitle = content.getChildAt(1) as TextView
                val release = row.release
                val continuing = row.kind == Kind.CONTINUE
                val poster = row.kind == Kind.POSTER
                val webPoster = poster
                val schedulePoster = poster && row.selected in listOf(1, 3)
                val cataloguePoster = poster && row.selected !in listOf(1, 3)
                val image = box.getChildAt(0) as ImageView
                image.layoutParams =
                    FrameLayout.LayoutParams(
                        -1,
                        if (poster) view.cardDp(198) else view.cardDp(if (continuing) 150 else 130),
                        Gravity.TOP,
                    )
                image.scaleType =
                    if (poster && row.selected != 2) ImageView.ScaleType.FIT_CENTER
                    else ImageView.ScaleType.CENTER_CROP
                image.background = shape(0xFF212121.toInt(), view.dp(12).toFloat())
                image.clipToOutline = true
                if (continuing && row.image == null) {
                    image.scaleType = ImageView.ScaleType.FIT_CENTER
                    image.load(R.drawable.ic_aniliberty)
                } else if (row.kind != Kind.FEATURE)
                    image.load(if (poster) release?.poster else row.image)
                box.getChildAt(1).visibility = View.GONE
                title.label(release?.title ?: row.title, if (homeCard) 17f else 14f, WHITE, true)
                title.maxLines = 2
                title.hyphenationFrequency = android.text.Layout.HYPHENATION_FREQUENCY_NONE
                if (poster || continuing)
                    androidx.core.widget.TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                        title,
                        12,
                        if (row.selected == 2) 20 else if (homeCard) 17 else 14,
                        1,
                        android.util.TypedValue.COMPLEX_UNIT_SP,
                    )
                val short =
                    if (release != null)
                        listOf(
                                release.year,
                                release.type,
                                release.latestEpisode?.let { "Эп. ${number(it.ordinal)}" }.orEmpty(),
                            )
                            .filter(String::isNotBlank)
                            .joinToString(" · ")
                    else row.subtitle
                val full =
                    if (release != null)
                        listOf(release.metadata, release.genres.take(2).joinToString(" · "))
                            .filter(String::isNotBlank)
                            .joinToString(" · ")
                    else row.subtitle
                subtitle.label(
                    if (box.hasFocus()) full else short,
                    if (homeCard) 14f else 12f,
                    MUTED,
                )
                subtitle.maxLines = 1
                subtitle.visibility =
                    if (short.isBlank() && full.isBlank()) View.GONE else View.VISIBLE
                content.layoutParams =
                    FrameLayout.LayoutParams(
                        -1,
                        view.cardDp(if (continuing) 86 else 66),
                        Gravity.BOTTOM,
                    )
                content.setPadding(view.dp(4), view.dp(6), view.dp(4), 0)

                if (continuing) {
                    val item = row.watching!!
                    subtitle.maxLines = 2
                    (box.getChildAt(3) as android.widget.ProgressBar).progress =
                        if (item.duration > 0)
                            (item.position * 1000 / item.duration).toInt().coerceIn(0, 1000)
                        else 0
                }
                content.setBackgroundColor(Color.TRANSPARENT)
                box.setOnFocusChangeListener { _, focused ->
                    subtitle.text = if (focused) full else short
                    title.setTextColor(if (focused) WHITE else 0xFFDDDDDD.toInt())
                    box.animate().cancel()
                    box.scaleX = 1f
                    box.scaleY = 1f
                }
                if (webPoster) {
                    box.layoutParams =
                        box.layoutParams.apply {
                            height =
                                if (shelfMode && row.selected == 2) (view.cardDp(144) / .7f).toInt()
                                else if (row.selected == 2 || (row.homeStyle && poster))
                                    ((view.resources.displayMetrics.widthPixels - view.dp(156)) /
                                            4 /
                                            .7f)
                                        .toInt()
                                else view.cardDp(if (cataloguePoster) 270 else 206)
                        }
                    if (row.selected == 2) title.textSize = 18f
                    image.layoutParams = FrameLayout.LayoutParams(-1, -1)
                    box.getChildAt(1).visibility = View.GONE
                    content.layoutParams = FrameLayout.LayoutParams(-1, -1)
                    content.gravity = Gravity.CENTER
                    content.setPadding(view.dp(10), view.dp(10), view.dp(10), view.dp(10))
                    title.gravity = Gravity.CENTER
                    title.maxLines = 4
                    subtitle.gravity = Gravity.CENTER
                    subtitle.maxLines = 4
                    val publicationTime =
                        relativeUpdateLabel(
                            release?.latestEpisode?.updatedAt?.ifBlank { release.updatedAt }
                                ?: release?.updatedAt.orEmpty()
                        )
                    val yearDetails =
                        listOf(
                                release?.year.orEmpty(),
                                release?.season.orEmpty(),
                                release
                                    ?.age
                                    ?.let { Regex("[0-9]+").find(it)?.value?.plus("+") }
                                    .orEmpty(),
                            )
                            .filter(String::isNotBlank)
                            .joinToString(" · ")
                    val publicationAndYear =
                        listOf(publicationTime, yearDetails)
                            .filter(String::isNotBlank)
                            .joinToString("\n")
                    subtitle.text =
                        android.text.SpannableString(publicationAndYear).apply {
                            if (publicationTime.isNotBlank()) {
                                setSpan(
                                    android.text.style.RelativeSizeSpan(0.75f),
                                    0,
                                    publicationTime.length,
                                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                                )
                            }
                        }
                    subtitle.maxLines = 2
                    val genresLine =
                        (content.getChildAt(2) as? TextView)
                            ?: TextView(view.context).also {
                                content.addView(
                                    it,
                                    LinearLayout.LayoutParams(-1, -2).apply {
                                        topMargin = view.dp(3)
                                    },
                                )
                            }
                    genresLine.label(
                        release?.genres?.joinToString(" • ").orEmpty(),
                        if (homeCard) 14f else 12f,
                        MUTED,
                    )
                    genresLine.gravity = Gravity.CENTER
                    genresLine.maxLines = 1
                    genresLine.setSingleLine(true)
                    genresLine.ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
                    genresLine.marqueeRepeatLimit = -1
                    genresLine.isHorizontalFadingEdgeEnabled = true
                    genresLine.setFadingEdgeLength(view.dp(10))
                    genresLine.isFocusable = false
                    val episodeBadge =
                        (box.getChildAt(3) as? TextView)
                            ?: TextView(view.context).also {
                                box.addView(
                                    it,
                                    FrameLayout.LayoutParams(
                                            -2,
                                            view.dp(28),
                                            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                                        )
                                        .apply { topMargin = view.dp(10) },
                                )
                            }
                    episodeBadge.label(
                        if (schedulePoster) scheduleEpisodeLabel(release)
                        else if (cataloguePoster) release?.type?.ifBlank { "Аниме" } ?: "Аниме"
                        else
                            release?.latestEpisode?.let { "${number(it.ordinal)} эпизод" }
                                ?: "Релиз",
                        12f,
                        WHITE,
                        true,
                    )
                    episodeBadge.gravity = Gravity.CENTER
                    episodeBadge.setPadding(view.dp(10), 0, view.dp(10), 0)
                    episodeBadge.background =
                        shape(
                            if (schedulePoster) 0xCC181818.toInt() else 0x1AFFFFFF,
                            view.dp(6).toFloat(),
                        )
                    episodeBadge.isFocusable = false
                    val updateLabel =
                        if (!cataloguePoster) {
                            val label =
                                (box.getChildAt(4) as? TextView)
                                    ?: TextView(view.context).also { box.addView(it) }
                            label.layoutParams =
                                FrameLayout.LayoutParams(-1, view.dp(16), Gravity.TOP).apply {
                                    topMargin = view.dp(40)
                                    marginStart = view.dp(8)
                                    marginEnd = view.dp(8)
                                }
                            label.label(
                                relativeUpdateLabel(
                                    release?.latestEpisode?.updatedAt?.ifBlank { release.updatedAt }
                                        ?: release?.updatedAt.orEmpty()
                                ),
                                10f,
                                MUTED,
                            )
                            label.gravity = Gravity.CENTER
                            label.maxLines = 1
                            label.includeFontPadding = false
                            label
                        } else null
                    val statusBadge =
                        if (cataloguePoster) {
                            episodeBadge.textSize = 12f
                            episodeBadge.layoutParams =
                                FrameLayout.LayoutParams(
                                        -2,
                                        view.dp(28),
                                        Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                                    )
                                    .apply { topMargin = view.dp(10) }
                            val status =
                                (box.getChildAt(4) as? TextView)
                                    ?: TextView(view.context).also { box.addView(it) }
                            status.layoutParams =
                                FrameLayout.LayoutParams(-1, view.dp(16), Gravity.TOP).apply {
                                    topMargin = view.dp(40)
                                    marginStart = view.dp(8)
                                    marginEnd = view.dp(8)
                                }
                            status.label(
                                if (release?.ongoing == true) "Онгоинг" else "Завершён",
                                10f,
                                MUTED,
                            )
                            status.gravity = Gravity.CENTER
                            status.includeFontPadding = false
                            status.background = null
                            status.isFocusable = false
                            status
                        } else {
                            episodeBadge.layoutParams =
                                FrameLayout.LayoutParams(
                                        -2,
                                        view.dp(28),
                                        Gravity.TOP or Gravity.END,
                                    )
                                    .apply {
                                        topMargin = view.dp(10)
                                        marginEnd = view.dp(8)
                                    }
                            null
                        }
                    content.setPadding(view.dp(10), view.dp(44), view.dp(10), view.dp(12))
                    title.layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
                    // Keep the update timestamp and year/season/age on separate visible lines.
                    subtitle.maxLines = 2
                    subtitle.ellipsize = null
                    fun focusPoster(focused: Boolean) {
                        box.findViewWithTag<TextView>("favorite-count")?.visibility = View.GONE
                        box.findViewWithTag<TextView>("schedule-favorites")?.visibility = View.GONE
                        box.findViewWithTag<LinearLayout>("poster-meta-badge")?.visibility =
                            if (focused || cataloguePoster || schedulePoster || row.isFavorite || row.collectionType != null)
                                View.VISIBLE
                            else View.GONE
                        content.visibility = if (focused) View.VISIBLE else View.GONE
                        genresLine.visibility =
                            if (focused && genresLine.text.isNotBlank()) View.VISIBLE else View.GONE
                        genresLine.isSelected = focused
                        if (cataloguePoster) {
                            box.getChildAt(1).visibility = View.GONE
                            content.gravity = if (focused) Gravity.CENTER else Gravity.BOTTOM
                            content.setPadding(
                                view.dp(10),
                                view.dp(
                                    if (focused && release?.ongoing != null) 56
                                    else if (focused) 44 else 10
                                ),
                                view.dp(10),
                                view.dp(12),
                            )
                            title.layoutParams =
                                LinearLayout.LayoutParams(
                                    -1,
                                    if (focused) 0 else -2,
                                    if (focused) 1f else 0f,
                                )
                            title.maxLines = if (focused) 4 else 3
                            subtitle.visibility = if (focused) View.VISIBLE else View.GONE
                        }
                        episodeBadge.visibility =
                            if (focused || schedulePoster) View.VISIBLE else View.GONE
                        statusBadge?.visibility =
                            if (focused && release?.ongoing != null) View.VISIBLE else View.GONE
                        updateLabel?.visibility = View.GONE
                        if (!cataloguePoster) {
                            content.setPadding(
                                view.dp(10),
                                view.dp(44),
                                view.dp(10),
                                view.dp(12),
                            )
                            title.gravity = Gravity.CENTER
                            title.includeFontPadding = false
                        }
                        image.colorFilter =
                            if (focused)
                                android.graphics.ColorMatrixColorFilter(
                                    floatArrayOf(
                                        .045f,
                                        0f,
                                        0f,
                                        0f,
                                        12.75f,
                                        0f,
                                        .045f,
                                        0f,
                                        0f,
                                        12.75f,
                                        0f,
                                        0f,
                                        .045f,
                                        0f,
                                        12.75f,
                                        0f,
                                        0f,
                                        0f,
                                        1f,
                                        0f,
                                    )
                                )
                            else null
                    }
                    if (row.selected == 2) {
                        val count =
                            box.findViewWithTag<TextView>("episode-count")
                                ?: TextView(view.context).also {
                                    it.tag = "episode-count"
                                    box.addView(it)
                                }
                        count.layoutParams =
                            FrameLayout.LayoutParams(-2, view.dp(28), Gravity.TOP or Gravity.END)
                                .apply {
                                    topMargin = view.dp(10)
                                    marginEnd = view.dp(8)
                                }
                        count.label(
                            release?.catalogueEpisodeCount?.let { "$it эп." }.orEmpty(),
                            12f,
                            WHITE,
                            true,
                        )
                        count.gravity = Gravity.CENTER
                        count.includeFontPadding = false
                        count.setPadding(view.dp(10), 0, view.dp(10), 0)
                        count.background = shape(0xCC181818.toInt(), view.dp(6).toFloat())
                        count.visibility =
                            if (release?.catalogueEpisodeCount != null) View.VISIBLE else View.GONE
                        count.isFocusable = false
                    } else box.findViewWithTag<TextView>("episode-count")?.visibility = View.GONE
                    val favorites =
                        box.findViewWithTag<TextView>("favorite-count")
                            ?: TextView(view.context).also {
                                it.tag = "favorite-count"
                                box.addView(it)
                            }
                    favorites.layoutParams =
                        FrameLayout.LayoutParams(-2, view.dp(if (cataloguePoster) 28 else 24), Gravity.TOP or Gravity.START)
                            .apply {
                                topMargin = view.dp(10)
                                marginStart = view.dp(8)
                            }
                    favorites.label(
                        release?.favoriteCount?.let(::compactCount) ?: if (cataloguePoster) "—" else "",
                        if (cataloguePoster) 12f else 10f,
                        WHITE,
                        true,
                    )
                    favorites.gravity = Gravity.CENTER_VERTICAL
                    favorites.includeFontPadding = false
                    favorites.isFocusable = false
                    favorites.setPadding(view.dp(if (cataloguePoster) 8 else 6), 0, view.dp(if (cataloguePoster) 8 else 6), 0)
                    favorites.compoundDrawablePadding = view.dp(3)
                    favorites.setCompoundDrawablesRelative(
                        view.context.getDrawable(R.drawable.ic_favorite)?.mutate()?.apply {
                            setTint(if (row.isFavorite) 0xFFFFD54F.toInt() else WHITE)
                            setBounds(0, 0, view.dp(13), view.dp(13))
                        },
                        null,
                        null,
                        null,
                    )
                    favorites.background =
                        shape(
                            if (cataloguePoster) 0xCC181818.toInt() else 0x1AFFFFFF,
                            view.dp(if (cataloguePoster) 6 else 4).toFloat(),
                        )
                    favorites.contentDescription =
                        release?.favoriteCount?.let { "В избранном у $it пользователей" }.orEmpty()
                    box.findViewWithTag<TextView>("schedule-favorites")?.visibility =
                        if (schedulePoster) View.VISIBLE else View.GONE
                    if (schedulePoster) {
                        val rating =
                            box.findViewWithTag<TextView>("schedule-favorites")
                                ?: TextView(view.context).also {
                                    it.tag = "schedule-favorites"
                                    box.addView(it)
                                }
                        rating.layoutParams =
                            FrameLayout.LayoutParams(-2, view.dp(28), Gravity.TOP or Gravity.START)
                                .apply {
                                    topMargin = view.dp(10)
                                    marginStart = view.dp(8)
                                }
                        rating.label(
                            release?.favoriteCount?.let(::compactCount) ?: "—",
                            12f,
                            WHITE,
                            true,
                        )
                        rating.compoundDrawablePadding = view.dp(4)
                        rating.setCompoundDrawablesRelative(
                            view.context.getDrawable(R.drawable.ic_favorite)?.mutate()?.apply {
                                setTint(if (row.isFavorite) 0xFFFFD54F.toInt() else WHITE)
                                setBounds(0, 0, view.dp(13), view.dp(13))
                            },
                            null,
                            null,
                            null,
                        )
                        rating.setPadding(view.dp(8), 0, view.dp(8), 0)
                        rating.gravity = Gravity.CENTER
                        rating.background = shape(0xCC181818.toInt(), view.dp(6).toFloat())
                        rating.isFocusable = false
                        rating.contentDescription =
                            release?.favoriteCount?.let { "В избранном у $it пользователей" }
                                ?: "Количество добавлений в избранное неизвестно"
                    }
                    val posterBadge =
                        box.findViewWithTag<LinearLayout>("poster-meta-badge")
                            ?: LinearLayout(view.context).also {
                                it.tag = "poster-meta-badge"
                                box.addView(it)
                            }
                    posterBadge.layoutParams =
                        FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.START).apply {
                            topMargin = view.dp(10)
                            marginStart = view.dp(8)
                        }
                    posterBadge.orientation = LinearLayout.VERTICAL
                    posterBadge.gravity = Gravity.CENTER_HORIZONTAL
                    posterBadge.setPadding(view.dp(8), view.dp(6), view.dp(8), view.dp(6))
                    posterBadge.background = shape(0xCC181818.toInt(), view.dp(6).toFloat())
                    posterBadge.isFocusable = false
                    posterBadge.removeAllViews()
                    posterBadge.addView(
                        TextView(view.context).apply {
                            label(release?.favoriteCount?.let(::compactCount) ?: "—", 12f, WHITE, true)
                            gravity = Gravity.CENTER
                            includeFontPadding = false
                            compoundDrawablePadding = view.dp(4)
                            setCompoundDrawablesRelative(
                                view.context.getDrawable(R.drawable.ic_favorite)?.mutate()?.apply {
                                    setTint(if (row.isFavorite) CollectionVisuals.FAVORITE_COLOR else WHITE)
                                    setBounds(0, 0, view.dp(14), view.dp(14))
                                },
                                null,
                                null,
                                null,
                            )
                        },
                        LinearLayout.LayoutParams(-2, -2),
                    )
                    row.collectionType?.let { type ->
                        posterBadge.addView(
                            ImageView(view.context).apply {
                                setImageResource(type.icon)
                                setColorFilter(type.color)
                                contentDescription = "Коллекция: ${type.title}"
                                isFocusable = false
                            },
                            LinearLayout.LayoutParams(view.dp(20), view.dp(20)).apply {
                                topMargin = view.dp(5)
                            },
                        )
                    }
                    posterBadge.contentDescription =
                        listOfNotNull(
                            release?.favoriteCount?.let { "В избранном у $it пользователей" },
                            row.collectionType?.let { "Коллекция: ${it.title}" },
                        ).joinToString(". ")
                    focusPoster(box.hasFocus())
                    box.setOnFocusChangeListener { _, focused ->
                        focusPoster(focused)
                        box.animate().cancel()
                        box.scaleX = 1f
                        box.scaleY = 1f
                    }
                } else {
                    image.clearColorFilter()
                    content.visibility = View.VISIBLE
                    content.gravity = Gravity.TOP
                    title.gravity = Gravity.START
                    subtitle.gravity = Gravity.START
                    if (poster) {
                        box.layoutParams = box.layoutParams.apply { height = view.cardDp(270) }
                        title.layoutParams = LinearLayout.LayoutParams(-1, -2)
                        for (index in 3 until box.childCount) box.getChildAt(index).visibility =
                            View.GONE
                    }
                }
                if (continuing) {
                    // Preserve the original 272 × 150 episode preview; overlay text without
                    // stretching it.
                    box.layoutParams = box.layoutParams.apply { height = view.cardDp(150) }
                    image.layoutParams = FrameLayout.LayoutParams(-1, -1)
                    box.getChildAt(1).apply {
                        background = GenreShade()
                        visibility = View.VISIBLE
                    }
                    content.layoutParams = FrameLayout.LayoutParams(-1, -1)
                    content.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    content.setPadding(view.dp(12), view.dp(12), view.dp(12), view.dp(16))
                    title.gravity = Gravity.CENTER
                    title.maxLines = 3
                    subtitle.gravity = Gravity.CENTER
                    subtitle.maxLines = 5
                    subtitle.text = row.subtitle
                    (box.getChildAt(3) as android.widget.ProgressBar).layoutParams =
                        FrameLayout.LayoutParams(-1, view.dp(4), Gravity.BOTTOM)
                    val item = row.watching!!
                    val badge =
                        (box.getChildAt(4) as? TextView)
                            ?: TextView(view.context).also {
                                box.addView(
                                    it,
                                    FrameLayout.LayoutParams(
                                            -2,
                                            view.dp(28),
                                            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                                        )
                                        .apply { topMargin = view.dp(8) },
                                )
                            }
                    badge.label("${number(item.ordinal)} эпизод", 12f, WHITE, true)
                    badge.gravity = Gravity.CENTER
                    badge.setPadding(view.dp(10), 0, view.dp(10), 0)
                    badge.background = shape(0x1AFFFFFF, view.dp(6).toFloat())
                    badge.isFocusable = false
                    subtitle.text =
                        when {
                            item.nextUp -> "Следующая серия"
                            item.duration > 0 ->
                                "Осталось ${(item.remaining + 59_999L) / 60_000L} мин"
                            else -> "Продолжить"
                        }
                    subtitle.maxLines = 2
                    subtitle.textSize = 12f
                    fun focusContinue(focused: Boolean) {
                        title.visibility = View.VISIBLE
                        title.maxLines = if (focused) 3 else 2
                        title.layoutParams =
                            LinearLayout.LayoutParams(
                                -1,
                                if (focused) 0 else -2,
                                if (focused) 1f else 0f,
                            )
                        content.setPadding(
                            view.dp(12),
                            view.dp(if (focused) 38 else 12),
                            view.dp(12),
                            view.dp(16),
                        )
                        badge.visibility = if (focused) View.VISIBLE else View.GONE
                        subtitle.visibility = if (focused) View.VISIBLE else View.GONE
                        image.colorFilter =
                            if (focused)
                                android.graphics.ColorMatrixColorFilter(
                                    floatArrayOf(
                                        .2f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        .2f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        .2f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        1f,
                                        0f,
                                    )
                                )
                            else null
                    }
                    focusContinue(box.hasFocus())
                    box.setOnFocusChangeListener { _, focused ->
                        focusContinue(focused)
                        box.animate().cancel()
                        box.scaleX = 1f
                        box.scaleY = 1f
                    }
                }
                if (row.kind == Kind.FEATURE) {
                    image.scaleType = ImageView.ScaleType.FIT_CENTER
                    image.layoutParams = FrameLayout.LayoutParams(-1, -1)
                    image.load(row.image)
                    box.getChildAt(1).apply {
                        background = GenreShade()
                        visibility = View.VISIBLE
                    }
                    content.layoutParams = FrameLayout.LayoutParams(-1, -1)
                    content.gravity = Gravity.CENTER
                    content.setPadding(view.dp(14), view.dp(14), view.dp(14), view.dp(14))
                    title.maxLines = 6
                    title.gravity = Gravity.CENTER
                    subtitle.maxLines = 4
                    subtitle.gravity = Gravity.CENTER
                    subtitle.text = row.subtitle
                    fun videoFocus(focused: Boolean) {
                        image.colorFilter =
                            if (focused)
                                android.graphics.ColorMatrixColorFilter(
                                    floatArrayOf(
                                        .15f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        .15f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        .15f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        0f,
                                        1f,
                                        0f,
                                    )
                                )
                            else null
                        content.visibility = if (focused) View.VISIBLE else View.GONE
                    }
                    videoFocus(box.hasFocus())
                    box.setOnFocusChangeListener { _, focused -> videoFocus(focused) }
                }
                box.contentDescription =
                    listOf(release?.title ?: row.title, short)
                        .filter(String::isNotBlank)
                        .joinToString(". ")
                box.setOnClickListener {
                    if (row.click != null) row.click.invoke()
                    else if (release != null) details(release)
                }
                box.setOnLongClickListener(
                    when {
                        row.longClick != null ->
                            View.OnLongClickListener {
                                row.longClick.invoke()
                                true
                            }
                        release != null ->
                            View.OnLongClickListener {
                                quickActions(release)
                                true
                            }
                        else -> null
                    }
                )
                box.isLongClickable = row.longClick != null || release != null
            }
            Kind.EPISODE -> {
                val box = view as FrameLayout
                val e = row.episode!!
                val watched = row.episodeProgress ?: progress(e.id)
                (box.getChildAt(0) as ImageView).apply {
                    contentDescription =
                        if (watched.first <= 0) "Превью скрыто до начала просмотра"
                        else "Превью серии"
                    if (watched.first <= 0) setImageDrawable(null)
                    load(e.preview) {
                        if (watched.first <= 0) {
                            allowHardware(false)
                            size(192, 108)
                            transformations(SpoilerBlurTransformation())
                        }
                    }
                }
                val completed = isEpisodeWatched(watched.first, watched.second, TvSettings(box.context).watchedPercent)
                val episodeTitle =
                    listOf(
                            row.title,
                            if (e.name.isBlank()) e.label else "${number(e.ordinal)} · ${e.name}",
                        )
                        .filter { it.isNotBlank() }
                        .joinToString("\n")
                val duration =
                    if (e.duration > 0) "%d:%02d".format(e.duration / 60, e.duration % 60) else ""
                val status =
                    when {
                        e.streams.isEmpty() -> "Пока недоступно"
                        completed -> "Просмотрено"
                        watched.first > 0 -> "Продолжить"
                        else -> ""
                    }
                (box.getChildAt(2) as TextView).apply {
                    label(
                        listOf(
                                episodeTitle,
                                listOf(duration, status)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · "),
                            )
                            .filter { it.isNotBlank() }
                            .joinToString("\n"),
                        16f,
                        WHITE,
                        true,
                    )
                    maxLines = 4
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
                (box.getChildAt(3) as android.widget.ProgressBar).progress =
                    if (watched.second > 0)
                        (watched.first * 1000 / watched.second).toInt().coerceIn(0, 1000)
                    else 0
                box.contentDescription =
                    listOf(episodeTitle, duration, status)
                        .filter { it.isNotBlank() }
                        .joinToString(". ")
                box.setOnClickListener { row.click?.invoke() ?: play(e) }
                box.setOnLongClickListener(
                    row.longClick?.let { callback ->
                        View.OnLongClickListener {
                            callback()
                            true
                        }
                    }
                )
                box.isLongClickable = row.longClick != null
            }
            Kind.SETTING,
            Kind.ACCOUNT_TILE -> {
                val box = view as LinearLayout
                box.layoutParams =
                    box.layoutParams.apply {
                        height = view.dp(if (row.kind == Kind.ACCOUNT_TILE) 108 else 76)
                    }
                box.setPadding(view.dp(20), view.dp(12), view.dp(20), view.dp(12))
                box.isFocusable = row.click != null
                box.isClickable = row.click != null
                box.webControlBackground()
                val icon = box.getChildAt(0) as ImageView
                icon.visibility = if (row.kind == Kind.ACCOUNT_TILE) View.VISIBLE else View.GONE
                if (row.kind == Kind.ACCOUNT_TILE) {
                    icon.setImageResource(row.selected)
                    icon.setColorFilter(
                        when (row.selected) {
                            R.drawable.ic_favorite -> CollectionVisuals.FAVORITE_COLOR
                            R.drawable.ic_collection_history -> CollectionVisuals.HISTORY_COLOR
                            else -> WHITE
                        }
                    )
                }
                val labels = box.getChildAt(1) as LinearLayout
                (labels.getChildAt(0) as TextView).apply {
                    label(row.title, 17f, WHITE, true)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
                (labels.getChildAt(1) as TextView).apply {
                    label(row.subtitle, 13f, MUTED)
                    maxLines = 2
                    visibility = if (row.subtitle.isBlank()) View.GONE else View.VISIBLE
                }
                (box.getChildAt(2) as TextView).apply {
                    label(row.favoriteTitle ?: "›", 15f, MUTED)
                    maxWidth = view.dp(240)
                }
                box.setOnClickListener { row.click?.invoke() }
            }
            Kind.HEADING -> {
                val box = view as LinearLayout
                box.layoutParams =
                    box.layoutParams.apply {
                        height =
                            if (
                                row.key == "section-heading" ||
                                    row.key.startsWith("period:") ||
                                    row.key == "library-heading"
                            )
                                view.dp(52)
                            else -2
                    }
                (box.getChildAt(0) as TextView).label(
                    row.title,
                    if (row.selected == 1) 24f else 18f,
                    WHITE,
                    true,
                )
                (box.getChildAt(1) as TextView).apply {
                    label(row.subtitle, 12f, MUTED)
                    visibility = if (row.subtitle.isBlank()) View.GONE else View.VISIBLE
                }
                val title = box.getChildAt(0) as TextView
                val refresh =
                    (box.getChildAt(2) as? android.widget.ImageButton)
                        ?: android.widget.ImageButton(view.context).also {
                            box.addView(it, LinearLayout.LayoutParams(view.dp(32), view.dp(32)))
                        }
                refresh.visibility = if (row.click != null) View.VISIBLE else View.GONE
                box.orientation =
                    if (row.click != null) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                box.gravity = Gravity.CENTER_VERTICAL
                title.layoutParams =
                    LinearLayout.LayoutParams(
                        if (row.click != null) 0 else -1,
                        -2,
                        if (row.click != null) 1f else 0f,
                    )
                refresh.setImageResource(R.drawable.ic_refresh)
                refresh.setColorFilter(WHITE)
                refresh.layoutParams =
                    LinearLayout.LayoutParams(view.dp(32), view.dp(32)).apply {
                        marginStart = view.dp(12)
                    }
                refresh.setPadding(view.dp(7), view.dp(7), view.dp(7), view.dp(7))
                if (row.selected == 1) title.includeFontPadding = false
                refresh.contentDescription = "Обновить: ${row.title}"
                refresh.webControlBackground()
                refresh.setOnClickListener { row.click?.invoke() }
                box.isFocusable = false
                box.isClickable = false
            }
            Kind.TEXT,
            Kind.ACTION -> {
                val t = view as TextView
                t.label(
                    listOf(row.title, row.subtitle).filter(String::isNotBlank).joinToString("\n"),
                    16f,
                    if (row.kind == Kind.TEXT) MUTED else WHITE,
                )
                t.setPadding(view.dp(16), view.dp(8), view.dp(16), view.dp(8))
                t.isFocusable = row.click != null || inContent
                t.isClickable = row.click != null
                t.background = null
                if (t.isFocusable) t.webControlBackground()
                if (row.click != null) {
                    t.webControlBackground()
                    t.setPadding(view.dp(12), view.dp(10), view.dp(12), view.dp(10))
                }
                t.setOnClickListener(
                    row.click?.let { callback -> View.OnClickListener { callback() } }
                )
                t.isClickable = row.click != null
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder.itemView is RecyclerView) attachedShelves.add(holder.itemView as RecyclerView)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder.itemView as? RecyclerView)?.let {
            saveShelf(it)
            attachedShelves.remove(it)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder.itemView as? RecyclerView)?.let {
            saveShelf(it)
            attachedShelves.remove(it)
        }
        val view = holder.itemView
        if (view is FrameLayout) (view.getChildAt(0) as? ImageView)?.load(null)
        // MORE/REFRESH contain vector resources, not Coil image requests.
        if (view is LinearLayout && holder.itemViewType == Kind.HERO.ordinal)
            (view.getChildAt(0) as? ImageView)?.load(null)
        view.animate().cancel()
        view.scaleX = 1f
        view.scaleY = 1f
        view.setOnClickListener(null)
        view.setOnKeyListener(null)
        view.setOnLongClickListener(null)
        view.onFocusChangeListener = null
    }
}

/** Home previews contain at most nine real cards and one explicit full-section destination. */
fun shelfCards(
    key: String,
    title: String,
    items: List<Row>,
    wide: Boolean = false,
    style: Int = if (wide) 1 else 0,
    openAll: () -> Unit,
): List<Row> = items.take(9) + Row("$key:all", Kind.MORE, title, selected = style, click = openAll)

/** Same three stops as genresProxy: transparent, 20% black at 70%, opaque bottom. */
private class GenreShade : android.graphics.drawable.Drawable() {
    private val paint = android.graphics.Paint()

    override fun onBoundsChange(bounds: android.graphics.Rect) {
        paint.shader =
            android.graphics.LinearGradient(
                0f,
                bounds.top.toFloat(),
                0f,
                bounds.bottom.toFloat(),
                intArrayOf(0x00101010, 0x33101010, Color.BLACK),
                floatArrayOf(0f, .7f, 1f),
                android.graphics.Shader.TileMode.CLAMP,
            )
    }

    override fun draw(canvas: android.graphics.Canvas) {
        canvas.drawRect(bounds, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(filter: android.graphics.ColorFilter?) {
        paint.colorFilter = filter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity() = android.graphics.PixelFormat.TRANSLUCENT
}

/**
 * Discovery previews contain three franchises or four genres plus two independently focusable
 * actions.
 */
fun discoveryShelfCards(
    key: String,
    title: String,
    items: List<Row>,
    style: Int,
    busy: Boolean = false,
    failed: Boolean = false,
    refresh: () -> Unit,
    openAll: () -> Unit,
): List<Row> =
    items.take(if (style == 3) 3 else 4) +
        listOf(
            Row(
                "$key:refresh",
                Kind.REFRESH,
                if (busy) "Обновление…" else if (failed) "Повторить обновление" else "Обновить",
                selected = style,
                click = if (busy) null else refresh,
            ),
            Row("$key:all", Kind.MORE, title, selected = style, click = openAll),
        )
