package ru.radiationx.data.interactors

import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.apinext.models.filters.CatalogFilterData
import ru.radiationx.data.apinext.models.filters.CatalogFilterForm
import ru.radiationx.data.apinext.models.filters.CollectionsFilterData
import ru.radiationx.data.apinext.models.filters.CollectionsFilterForm
import ru.radiationx.data.apinext.models.filters.FavoritesFilterData
import ru.radiationx.data.apinext.models.filters.FavoritesFilterForm
import ru.radiationx.data.apinext.models.filters.FilterItem
import ru.radiationx.data.apinext.models.filters.FormItem
import ru.radiationx.data.entity.domain.Paginated
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.repository.CatalogRepository
import ru.radiationx.data.repository.CollectionsRepository
import ru.radiationx.data.repository.FavoriteRepository
import toothpick.InjectConstructor

@InjectConstructor
class FilterInteractor(
    private val collectionsRepository: CollectionsRepository,
    private val favoriteRepository: FavoriteRepository,
    private val catalogRepository: CatalogRepository
) {

    private companion object {
        val collectionsFields = setOf(
            FieldType.Query,
            FieldType.AgeRating,
            FieldType.Genre,
            FieldType.ReleaseType,
            FieldType.Year
        )

        val favoritesFields = setOf(
            FieldType.Query,
            FieldType.AgeRating,
            FieldType.Genre,
            FieldType.ReleaseType,
            FieldType.Sorting,
            FieldType.Year
        )

        val catalogFields = setOf(
            FieldType.Query,
            FieldType.AgeRating,
            FieldType.Genre,
            FieldType.ProductionStatus,
            FieldType.PublishStatus,
            FieldType.ReleaseType,
            FieldType.Season,
            FieldType.Sorting,
            FieldType.YearsRange
        )
    }

    suspend fun getFilterData(type: FilterType): FilterData = when (type) {
        FilterType.Collections -> {
            collectionsRepository.getFilterData().toData(type, collectionsFields)
        }

        FilterType.Favorites -> {
            favoriteRepository.getFilterData().toData(type, favoritesFields)
        }

        FilterType.Catalog -> {
            catalogRepository.getFilterData().toData(type, catalogFields)
        }
    }

    suspend fun getReleases(
        filterType: FilterType,
        page: Int,
        form: FilterForm,
        collectionType: CollectionType?
    ): Paginated<Release> = when (filterType) {
        FilterType.Collections -> {
            requireNotNull(collectionType) {
                "CollectionType is null"
            }
            collectionsRepository.getReleases(collectionType, page, form.toCollections())
        }

        FilterType.Favorites -> {
            favoriteRepository.getReleases(page, form.toFavorites())
        }

        FilterType.Catalog -> {
            catalogRepository.getReleases(page, form.toCatalog())
        }
    }

    private fun FilterForm.toCollections(): CollectionsFilterForm = CollectionsFilterForm(
        query = query,
        ageRatings = ageRatings,
        genres = genres,
        types = types,
        years = years
    )

    private fun FilterForm.toFavorites(): FavoritesFilterForm = FavoritesFilterForm(
        query = query,
        ageRatings = ageRatings,
        genres = genres,
        types = types,
        sorting = sorting,
        years = years
    )

    private fun FilterForm.toCatalog(): CatalogFilterForm = CatalogFilterForm(
        query = query,
        ageRatings = ageRatings,
        genres = genres,
        productionStatuses = productionStatuses,
        publishStatuses = publishStatuses,
        types = types,
        seasons = seasons,
        sorting = sorting,
        yearsRange = yearsRange
    )

    private fun CollectionsFilterData.toData(
        type: FilterType,
        fields: Set<FieldType>
    ): FilterData = FilterData(
        type = type,
        fields = fields,
        ageRatings = ageRatings,
        genres = genres,
        productionStatuses = null,
        publishStatuses = null,
        types = types,
        seasons = null,
        sortings = null,
        years = years
    )

    private fun FavoritesFilterData.toData(
        type: FilterType,
        fields: Set<FieldType>
    ): FilterData = FilterData(
        type = type,
        fields = fields,
        ageRatings = ageRatings,
        genres = genres,
        productionStatuses = null,
        publishStatuses = null,
        types = types,
        seasons = null,
        sortings = sortings,
        years = years
    )

    private fun CatalogFilterData.toData(
        type: FilterType,
        fields: Set<FieldType>
    ): FilterData = FilterData(
        type = type,
        fields = fields,
        ageRatings = ageRatings,
        genres = genres,
        productionStatuses = productionStatuses,
        publishStatuses = publishStatuses,
        types = types,
        seasons = seasons,
        sortings = sortings,
        years = years
    )
}


enum class FilterType {
    Collections,
    Favorites,
    Catalog
}


enum class FieldType {
    Query,
    AgeRating,
    Genre,
    ProductionStatus,
    PublishStatus,
    ReleaseType,
    Season,
    Sorting,
    Year,
    YearsRange
}

data class FilterData(
    val type: FilterType,
    val fields: Set<FieldType>,
    val ageRatings: List<FilterItem.Value>?,
    val genres: List<FilterItem.Genre>?,
    val productionStatuses: List<FilterItem.Value>?,
    val publishStatuses: List<FilterItem.Value>?,
    val types: List<FilterItem.Value>?,
    val seasons: List<FilterItem.Value>?,
    val sortings: List<FilterItem.Value>?,
    val years: List<FilterItem.Year>?,
)

data class FilterForm(
    val query: String,
    val ageRatings: Set<FormItem.Value>,
    val genres: Set<FormItem.Genre>,
    val productionStatuses: Set<FormItem.Value>,
    val publishStatuses: Set<FormItem.Value>,
    val types: Set<FormItem.Value>,
    val seasons: Set<FormItem.Value>,
    val sorting: FormItem.Value?,
    val years: Set<FormItem.Year>,
    val yearsRange: Pair<FormItem.Year, FormItem.Year>?
) {
    companion object {
        fun empty(): FilterForm = FilterForm(
            query = "",
            ageRatings = emptySet(),
            genres = emptySet(),
            productionStatuses = emptySet(),
            publishStatuses = emptySet(),
            types = emptySet(),
            seasons = emptySet(),
            sorting = null,
            years = emptySet(),
            yearsRange = null
        )
    }
}


