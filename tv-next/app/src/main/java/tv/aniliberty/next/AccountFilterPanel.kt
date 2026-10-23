package tv.aniliberty.next

import android.content.Context

class AccountFilterPanel(
    context: Context,
    initial: AccountFilter,
    private val references: Map<String, List<CatalogChoice>>,
    private val apply: (AccountFilter) -> Unit,
) : TvSidePanel(context, "Фильтры списка") {
    private var draft = initial

    init {
        build()
    }

    private fun selected(key: String) =
        when (key) {
            "genres" -> draft.genres
            "types" -> draft.types
            "years" -> draft.years
            else -> draft.ages
        }

    private fun update(key: String, values: Set<String>) {
        draft =
            when (key) {
                "genres" -> draft.copy(genres = values)
                "types" -> draft.copy(types = values)
                "years" -> draft.copy(years = values)
                else -> draft.copy(ages = values)
            }
    }

    private fun build() {
        heading.text = "Фильтры списка"
        body.removeAllViews()
        footer.removeAllViews()
        linkedMapOf(
                "genres" to "Жанры",
                "types" to "Формат",
                "years" to "Годы",
                "age-ratings" to "Возраст",
            )
            .forEach { (key, title) ->
                val choices = references[key].orEmpty()
                addChoice(
                    title +
                        "\n" +
                        choices
                            .filter { it.value in selected(key) }
                            .joinToString(" · ") { it.title }
                            .ifBlank { "Все" }
                ) {
                    heading.text = title
                    body.removeAllViews()
                    footer.removeAllViews()
                    var staged = selected(key)
                    choices.forEach { choice ->
                        lateinit var button: android.widget.TextView
                        button =
                            addChoice(choice.title, choice.value in staged) {
                                staged =
                                    if (choice.value in staged) staged - choice.value
                                    else staged + choice.value
                                button.text =
                                    (if (choice.value in staged) "✓  " else "") + choice.title
                            }
                    }
                    if (choices.isEmpty()) message("В этом списке пока нет вариантов")
                    addAction("Готово", true) {
                        update(key, staged)
                        build()
                        body.getChildAt(0)?.requestFocus()
                    }
                    body.getChildAt(0)?.requestFocus()
                }
            }
        addAction("Применить", true) {
            apply(draft)
            dismiss()
        }
        addAction("Сбросить") {
            draft = AccountFilter(sorting = draft.sorting)
            build()
            body.getChildAt(0)?.requestFocus()
        }
        addAction("Отмена") { dismiss() }
    }
}
