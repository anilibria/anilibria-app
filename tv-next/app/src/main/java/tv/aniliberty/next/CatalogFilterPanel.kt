package tv.aniliberty.next

import android.content.Context
import android.view.KeyEvent
import android.widget.TextView

class CatalogFilterPanel(
    context: Context,
    screen: Screen.Catalog,
    private val references: Map<String, List<CatalogChoice>>,
    private val apply: (CatalogFilter) -> Unit,
) : TvSidePanel(context, "Фильтры") {
    private var draft =
        screen.filters.copy(
            genres = screen.filters.genres + listOfNotNull(screen.genre?.toString())
        )
    private var focusIndex = 0
    private var editing: String? = null
    private var staged: Set<String> = emptySet()

    init {
        build()
    }

    private fun selection(key: String): Set<String> =
        when (key) {
            "genres" -> draft.genres
            "types" -> draft.types
            "seasons" -> draft.seasons
            "age-ratings" -> draft.ages
            "publish-statuses" -> draft.publish
            "production-statuses" -> draft.production
            else -> emptySet()
        }

    private fun change(key: String, values: Set<String>) {
        draft =
            when (key) {
                "genres" -> draft.copy(genres = values)
                "types" -> draft.copy(types = values)
                "seasons" -> draft.copy(seasons = values)
                "age-ratings" -> draft.copy(ages = values)
                "publish-statuses" -> draft.copy(publish = values)
                "production-statuses" -> draft.copy(production = values)
                else -> draft
            }
    }

    private fun build() {
        editing = null
        heading.text = "Фильтры"
        body.removeAllViews()
        footer.removeAllViews()
        val fields = catalogReferencePaths.filterKeys { it !in setOf("years", "sorting") }
        fields.forEach { (key, title) ->
            val choices = references[key].orEmpty()
            val summary =
                choices
                    .filter { it.value in selection(key) }
                    .joinToString(" · ") { it.title }
                    .ifBlank { "Все" }
            addChoice("$title\n$summary") {
                focusIndex = fields.keys.indexOf(key)
                editChoices(key, title, choices)
            }
        }
        listOf(true, false).forEach { from ->
            val value = if (from) draft.fromYear else draft.toYear
            addChoice((if (from) "Год от: " else "Год до: ") + value.ifBlank { "Любой" }) {
                focusIndex = fields.size + if (from) 0 else 1
                editYear(from)
            }
        }
        addAction("Применить", true) {
            apply(draft)
            dismiss()
        }
        addAction("Сбросить") {
            draft = CatalogFilter(sorting = draft.sorting)
            focusIndex = -1
            build()
            footer.getChildAt(1)?.requestFocus()
        }
        body.post { if (focusIndex >= 0) body.getChildAt(focusIndex)?.requestFocus() }
    }

    private fun editChoices(key: String, title: String, choices: List<CatalogChoice>) {
        editing = key
        staged = selection(key)
        heading.text = title
        body.removeAllViews()
        footer.removeAllViews()
        if (choices.isEmpty()) message("Вариантов пока нет")
        choices.forEach { choice ->
            lateinit var button: TextView
            button =
                addChoice(choice.title, choice.value in staged) {
                    staged =
                        if (choice.value in staged) staged - choice.value else staged + choice.value
                    button.text = if (choice.value in staged) "✓  ${choice.title}" else choice.title
                }
        }
        addAction("Готово", true) {
            change(key, staged)
            build()
        }
        addAction("Назад") { build() }
        body.post { body.getChildAt(0)?.requestFocus() }
    }

    private fun editYear(from: Boolean) {
        editing = if (from) "from-year" else "to-year"
        heading.text = if (from) "Год от" else "Год до"
        body.removeAllViews()
        footer.removeAllViews()
        val years =
            references["years"].orEmpty().filter {
                if (from) draft.toYear.isBlank() || it.value.toInt() <= draft.toYear.toInt()
                else draft.fromYear.isBlank() || it.value.toInt() >= draft.fromYear.toInt()
            }
        val current = if (from) draft.fromYear else draft.toYear
        (listOf(CatalogChoice("", "Любой")) + years).forEach { choice ->
            addChoice(choice.title, choice.value == current) {
                draft =
                    if (from) draft.copy(fromYear = choice.value)
                    else draft.copy(toYear = choice.value)
                build()
            }
        }
        addAction("Назад") { build() }
        body.post { body.getChildAt(0)?.requestFocus() }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && editing != null) {
            if (event.action == KeyEvent.ACTION_UP) build()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}
