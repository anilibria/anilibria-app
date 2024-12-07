package anilibria.apigen.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Response
import okhttp3.RequestBody
import okhttp3.ResponseBody
import com.squareup.moshi.Json

import anilibria.apigen.models.AccountsOtpAcceptPostRequest
import anilibria.apigen.models.AccountsOtpGetPostRequest
import anilibria.apigen.models.AccountsOtpLoginPostRequest
import anilibria.apigen.models.AccountsUsersAuthLoginPostRequest
import anilibria.apigen.models.AccountsUsersAuthPasswordForgetPostRequest
import anilibria.apigen.models.AccountsUsersAuthPasswordResetPostRequest
import anilibria.apigen.models.AccountsUsersMeCollectionsDeleteRequestInner
import anilibria.apigen.models.AccountsUsersMeCollectionsPostRequestInner
import anilibria.apigen.models.AccountsUsersMeCollectionsReleasesPostRequest
import anilibria.apigen.models.AccountsUsersMeFavoritesPostRequestInner
import anilibria.apigen.models.AccountsUsersMeFavoritesReleasesPostRequest
import anilibria.apigen.models.AccountsUsersMeViewsTimecodesDeleteRequestInner
import anilibria.apigen.models.AccountsUsersMeViewsTimecodesPostRequestInner
import anilibria.apigen.models.AnimeCatalogReleasesPostRequest
import anilibria.apigen.models.Commonsv1httpresponses422content
import anilibria.apigen.models.Enumsaccountsusersusercollectiontype
import anilibria.apigen.models.Enumsaccountsusersuserfavoritefiltersorting
import anilibria.apigen.models.Enumsaccountsusersusersocialtype
import anilibria.apigen.models.EnumsanimecatalogfilterproductionStatus
import anilibria.apigen.models.EnumsanimecatalogfilterpublishStatus
import anilibria.apigen.models.Enumsanimecatalogfiltersorting
import anilibria.apigen.models.EnumsanimereleasesreleaseageRating
import anilibria.apigen.models.Enumsanimereleasesreleaseseason
import anilibria.apigen.models.Enumsanimereleasesreleasetype
import anilibria.apigen.models.Modelsanimegenresv1genre
import anilibria.apigen.models.Modelsanimereleasesv1release
import anilibria.apigen.models.Modelsanimereleasesv1releasemember
import anilibria.apigen.models.Modelsteamsv1team
import anilibria.apigen.models.Modelsteamsv1teamrole
import anilibria.apigen.models.Modelsusersv1user
import anilibria.apigen.models.ResponsesApiV1AccountsUsersMeCollectionsReferencesAgeRatingsInner
import anilibria.apigen.models.ResponsesApiV1AccountsUsersMeCollectionsReferencesGenresInner
import anilibria.apigen.models.ResponsesApiV1AccountsUsersMeCollectionsReferencesTypesInner
import anilibria.apigen.models.ResponsesApiV1AccountsUsersMeFavoritesReferencesAgeRatingsInner
import anilibria.apigen.models.ResponsesApiV1AccountsUsersMeFavoritesReferencesSortingInner
import anilibria.apigen.models.ResponsesApiV1AccountsUsersMeFavoritesReferencesTypesInner
import anilibria.apigen.models.ResponsesApiV1AnimeCatalogReferencesAgeRatingsInner
import anilibria.apigen.models.ResponsesApiV1AnimeCatalogReferencesGenresInner
import anilibria.apigen.models.ResponsesApiV1AnimeCatalogReferencesProductionStatusesInner
import anilibria.apigen.models.ResponsesApiV1AnimeCatalogReferencesPublishStatusesInner
import anilibria.apigen.models.ResponsesApiV1AnimeReleasesLatestInner
import anilibria.apigen.models.ResponsesApiV1AnimeReleasesReleaseInner
import anilibria.apigen.models.ResponsesApiV1AnimeTorrentsReleaseTorrentsInner
import anilibria.apigen.models.ResponsesApiV1TeamsUsersInner
import anilibria.apigen.models.ResponsesV1AnimeCatalogReferencesSeasonsInner
import anilibria.apigen.models.ResponsesV1AnimeCatalogReferencesSortingInner
import anilibria.apigen.models.ResponsesV1AnimeCatalogReferencesTypesInner
import anilibria.apigen.models.Responsesapiv1accountsotpget
import anilibria.apigen.models.Responsesapiv1accountsotplogin
import anilibria.apigen.models.Responsesapiv1accountsusersmecollectionsidsitem
import anilibria.apigen.models.Responsesapiv1animecatalogreleases
import anilibria.apigen.models.Responsesapiv1animegenresitem
import anilibria.apigen.models.Responsesapiv1animegenresreleases
import anilibria.apigen.models.Responsesapiv1animereleasesepisode
import anilibria.apigen.models.Responsesapiv1animereleaseslist
import anilibria.apigen.models.Responsesapiv1animetorrent
import anilibria.apigen.models.Responsesapiv1animetorrents
import anilibria.apigen.models.Responsesv1accountsusersauthlogin
import anilibria.apigen.models.Responsesv1accountsusersauthlogout
import anilibria.apigen.models.Responsesv1accountsusersauthsocialauthenticate
import anilibria.apigen.models.Responsesv1accountsusersauthsociallogin
import anilibria.apigen.models.Responsesv1accountsuserscollectionsreleases
import anilibria.apigen.models.Responsesv1accountsusersmefavoritesreleases
import anilibria.apigen.models.Responsesv1accountsusersmeviewstimecodesitem
import anilibria.apigen.models.Responsesv1animefranchise
import anilibria.apigen.models.Responsesv1animefranchises
import anilibria.apigen.models.Responsesv1animefranchisesbyRelease
import anilibria.apigen.models.Responsesv1animefranchisesrandom
import anilibria.apigen.models.Responsesv1animeschedulenow
import anilibria.apigen.models.Responsesv1animescheduleweek
import anilibria.apigen.models.Responsesv1mediapromotions
import anilibria.apigen.models.Responsesv1mediavideos

interface DefaultApi {
    /**
     * POST accounts/otp/accept
     * Присоединяем пользователя к выданному OTP
     * Присоединяем пользователя к выданному одноразовому паролю
     * Responses:
     *  - 200: Успешная привязка пользователя к OTP
     *  - 401: Пользователь не авторизован
     *  - 404: OTP не найден
     *  - 422: Ошибка валидации входных параметров
     *
     * @param accountsOtpAcceptPostRequest 
     * @return [Unit]
     */
    @POST("accounts/otp/accept")
    suspend fun accountsOtpAcceptPost(@Body accountsOtpAcceptPostRequest: AccountsOtpAcceptPostRequest): Response<Unit>

    /**
     * POST accounts/otp/get
     * Запрашивает OTP
     * Запрашиваем новый одноразовый пароль
     * Responses:
     *  - 200: Данные OTP
     *  - 404: Не удалось создать OTP
     *  - 422: Ошибка валидации входных параметров
     *
     * @param accountsOtpGetPostRequest 
     * @return [Responsesapiv1accountsotpget]
     */
    @POST("accounts/otp/get")
    suspend fun accountsOtpGetPost(@Body accountsOtpGetPostRequest: AccountsOtpGetPostRequest): Response<Responsesapiv1accountsotpget>

    /**
     * POST accounts/otp/login
     * Авторизуемся по OTP
     * Авторизуемся по выданному одноразовому паролю
     * Responses:
     *  - 200: Токен авторизации
     *  - 401: OTP не привязан к пользователю
     *  - 404: OTP не найден
     *  - 422: Ошибка валидации входных параметров
     *
     * @param accountsOtpLoginPostRequest 
     * @return [Responsesapiv1accountsotplogin]
     */
    @POST("accounts/otp/login")
    suspend fun accountsOtpLoginPost(@Body accountsOtpLoginPostRequest: AccountsOtpLoginPostRequest): Response<Responsesapiv1accountsotplogin>

    /**
     * POST accounts/users/auth/login
     * Авторизация пользователя
     * Авторизация пользователя по логину и паролю. Создание сессии пользователя, выдача токена авторизации для использования в cookies или в Bearer Token
     * Responses:
     *  - 200: Токен авторизации
     *  - 422: Не указан логин и/или пароль
     *  - 401: Не удалось авторизоваться. Неправильные логин/пароль
     *
     * @param accountsUsersAuthLoginPostRequest Авторизационные данные
     * @return [Responsesv1accountsusersauthlogin]
     */
    @POST("accounts/users/auth/login")
    suspend fun accountsUsersAuthLoginPost(@Body accountsUsersAuthLoginPostRequest: AccountsUsersAuthLoginPostRequest): Response<Responsesv1accountsusersauthlogin>

    /**
     * POST accounts/users/auth/logout
     * Деавторизация пользователя
     * Деавторизовать пользователя
     * Responses:
     *  - 200: Результат деавторизации
     *  - 401: Не удалось деавторизоваться. Пользователь не авторизован
     *
     * @return [Responsesv1accountsusersauthlogout]
     */
    @POST("accounts/users/auth/logout")
    suspend fun accountsUsersAuthLogoutPost(): Response<Responsesv1accountsusersauthlogout>

    /**
     * POST accounts/users/auth/password/forget
     * Восстановление пароля
     * Отправление ссылки на восстановление забытого пароля
     * Responses:
     *  - 200: Письмо отправлено
     *
     * @param accountsUsersAuthPasswordForgetPostRequest Данные для восстановления
     * @return [Unit]
     */
    @POST("accounts/users/auth/password/forget")
    suspend fun accountsUsersAuthPasswordForgetPost(@Body accountsUsersAuthPasswordForgetPostRequest: AccountsUsersAuthPasswordForgetPostRequest): Response<Unit>

    /**
     * POST accounts/users/auth/password/reset
     * Сброс и установка нового пароля
     * Сброс и установка нового пароля
     * Responses:
     *  - 200: Пароль успешно сброшен
     *  - 404: Данные не найдены
     *  - 422: Ошибка валидации входных параметров
     *
     * @param accountsUsersAuthPasswordResetPostRequest Новые данные пользователя
     * @return [Unit]
     */
    @POST("accounts/users/auth/password/reset")
    suspend fun accountsUsersAuthPasswordResetPost(@Body accountsUsersAuthPasswordResetPostRequest: AccountsUsersAuthPasswordResetPostRequest): Response<Unit>

    /**
     * GET accounts/users/auth/social/authenticate
     * Аутентифицировать пользователя через социальные сети
     * Позволяет аутентифицировать авторизованного через социальную сеть пользователя
     * Responses:
     *  - 404: Ключ аутентификации не существует или пользователь еще не авторизован
     *  - 200: Токен авторизации
     *
     * @param state Ключ аутентификации
     * @return [Responsesv1accountsusersauthsocialauthenticate]
     */
    @GET("accounts/users/auth/social/authenticate")
    suspend fun accountsUsersAuthSocialAuthenticateGet(@Query("state") state: kotlin.String): Response<Responsesv1accountsusersauthsocialauthenticate>

    /**
     * GET accounts/users/auth/social/{provider}/login
     * Авторизация пользователя через социальные сети
     * Позволяет авторизовать пользователя через некоторые социальные сети
     * Responses:
     *  - 404: Указанный провайдер социальной сети не существует
     *  - 200: Данные для авторизации в социальной сети
     *
     * @param provider Провайдер социальной сети
     * @return [Responsesv1accountsusersauthsociallogin]
     */
    @GET("accounts/users/auth/social/{provider}/login")
    suspend fun accountsUsersAuthSocialProviderLoginGet(@Path("provider") provider: Enumsaccountsusersusersocialtype): Response<Responsesv1accountsusersauthsociallogin>

    /**
     * DELETE accounts/users/me/collections
     * Удалить релизы из коллекций
     * Удаляет релизы из соответствующих коллекций авторизованного пользователя
     * Responses:
     *  - 200: Релизы успешно удалены
     *  - 403: Необходимо авторизоваться
     *  - 422: Ошибка валидации входных параметров
     *
     * @param accountsUsersMeCollectionsDeleteRequestInner 
     * @return [kotlin.collections.List<kotlin.collections.List<Responsesapiv1accountsusersmecollectionsidsitem>>]
     */
    @DELETE("accounts/users/me/collections")
    suspend fun accountsUsersMeCollectionsDelete(@Body accountsUsersMeCollectionsDeleteRequestInner: kotlin.collections.List<AccountsUsersMeCollectionsDeleteRequestInner>): Response<kotlin.collections.List<kotlin.collections.List<Responsesapiv1accountsusersmecollectionsidsitem>>>

    /**
     * GET accounts/users/me/collections/ids
     * Список идентификаторов релизов добавленных в коллекции
     * Возвращает данные по идентификаторам релизов и типов коллекций авторизованного пользователя
     * Responses:
     *  - 200: Список идентификаторов релизов и типов коллекций
     *  - 403: Необходимо авторизоваться
     *
     * @return [kotlin.collections.List<kotlin.collections.List<Responsesapiv1accountsusersmecollectionsidsitem>>]
     */
    @GET("accounts/users/me/collections/ids")
    suspend fun accountsUsersMeCollectionsIdsGet(): Response<kotlin.collections.List<kotlin.collections.List<Responsesapiv1accountsusersmecollectionsidsitem>>>

    /**
     * POST accounts/users/me/collections
     * Добавить релизы в коллекции
     * Добавляет релизы в соответствующие коллекции авторизованного пользователя
     * Responses:
     *  - 200: Релизы успешно добавлены в коллекции
     *  - 403: Необходимо авторизоваться
     *  - 422: Ошибка валидации входных параметров
     *
     * @param accountsUsersMeCollectionsPostRequestInner 
     * @return [kotlin.collections.List<kotlin.collections.List<Responsesapiv1accountsusersmecollectionsidsitem>>]
     */
    @POST("accounts/users/me/collections")
    suspend fun accountsUsersMeCollectionsPost(@Body accountsUsersMeCollectionsPostRequestInner: kotlin.collections.List<AccountsUsersMeCollectionsPostRequestInner>): Response<kotlin.collections.List<kotlin.collections.List<Responsesapiv1accountsusersmecollectionsidsitem>>>

    /**
     * GET accounts/users/me/collections/references/age-ratings
     * Список возрастных рейтингов в коллекциях пользователя
     * Возвращает список возрастных рейтингов в коллекциях текущего пользователя
     * Responses:
     *  - 200: Список возрастных рейтингов
     *  - 403: Пользователь не авторизован
     *
     * @return [kotlin.collections.List<ResponsesApiV1AccountsUsersMeCollectionsReferencesAgeRatingsInner>]
     */
    @GET("accounts/users/me/collections/references/age-ratings")
    suspend fun accountsUsersMeCollectionsReferencesAgeRatingsGet(): Response<kotlin.collections.List<ResponsesApiV1AccountsUsersMeCollectionsReferencesAgeRatingsInner>>

    /**
     * GET accounts/users/me/collections/references/genres
     * Список жанров в коллекциях пользователя
     * Возвращает список жанров в коллекциях текущего пользователя
     * Responses:
     *  - 200: Список жанров
     *  - 403: Пользователь не авторизован
     *
     * @return [kotlin.collections.List<ResponsesApiV1AccountsUsersMeCollectionsReferencesGenresInner>]
     */
    @GET("accounts/users/me/collections/references/genres")
    suspend fun accountsUsersMeCollectionsReferencesGenresGet(): Response<kotlin.collections.List<ResponsesApiV1AccountsUsersMeCollectionsReferencesGenresInner>>

    /**
     * GET accounts/users/me/collections/references/types
     * Список типов в коллекциях пользователя
     * Возвращает список типов в коллекциях текущего пользователя
     * Responses:
     *  - 200: Список типов
     *  - 403: Пользователь не авторизован
     *
     * @return [kotlin.collections.List<ResponsesApiV1AccountsUsersMeCollectionsReferencesTypesInner>]
     */
    @GET("accounts/users/me/collections/references/types")
    suspend fun accountsUsersMeCollectionsReferencesTypesGet(): Response<kotlin.collections.List<ResponsesApiV1AccountsUsersMeCollectionsReferencesTypesInner>>

    /**
     * GET accounts/users/me/collections/references/years
     * Список годов в коллекциях пользователя
     * Возвращает список годов в коллекциях текущего пользователя
     * Responses:
     *  - 200: Список годов
     *  - 403: Пользователь не авторизован
     *
     * @return [kotlin.collections.List<kotlin.Int>]
     */
    @GET("accounts/users/me/collections/references/years")
    suspend fun accountsUsersMeCollectionsReferencesYearsGet(): Response<kotlin.collections.List<kotlin.Int>>

    /**
     * GET accounts/users/me/collections/releases
     * Список релизов добавленных в коллекцию [GET]
     * Возвращает данные по релизам из определенной коллекции авторизованного пользователя
     * Responses:
     *  - 200: Список релизов
     *  - 403: Необходимо авторизоваться
     *  - 422: Ошибка валидации входных параметров
     *
     * @param typeOfCollection Тип коллекции
     * @param page Номер страницы (optional)
     * @param limit Ограничение на количество элементов (optional)
     * @param fGenres Список идентификаторов жанров (optional)
     * @param fTypes Список типов релизов (optional)
     * @param fYears Минимальный год выхода релиза (optional)
     * @param fSearch Поисковый запрос (optional)
     * @param fAgeRatings Список возрастных рейтингов (optional)
     * @return [Responsesv1accountsuserscollectionsreleases]
     */
    @GET("accounts/users/me/collections/releases")
    suspend fun accountsUsersMeCollectionsReleasesGet(@Query("type_of_collection") typeOfCollection: Enumsaccountsusersusercollectiontype, @Query("page") page: kotlin.Int? = null, @Query("limit") limit: kotlin.Int? = null, @Query("f[genres]") fGenres: kotlin.String? = null, @Query("f[types]") fTypes: CSVParams? = null, @Query("f[years]") fYears: kotlin.String? = null, @Query("f[search]") fSearch: kotlin.String? = null, @Query("f[age_ratings]") fAgeRatings: CSVParams? = null): Response<Responsesv1accountsuserscollectionsreleases>

    /**
     * POST accounts/users/me/collections/releases
     * Список релизов добавленных в коллекцию [POST]
     * Возвращает данные по релизам из определенной коллекции авторизованного пользователя
     * Responses:
     *  - 200: Список релизов
     *  - 403: Необходимо авторизоваться
     *  - 422: Ошибка валидации входных параметров
     *
     * @param accountsUsersMeCollectionsReleasesPostRequest 
     * @return [Responsesv1accountsuserscollectionsreleases]
     */
    @POST("accounts/users/me/collections/releases")
    suspend fun accountsUsersMeCollectionsReleasesPost(@Body accountsUsersMeCollectionsReleasesPostRequest: AccountsUsersMeCollectionsReleasesPostRequest): Response<Responsesv1accountsuserscollectionsreleases>

    /**
     * DELETE accounts/users/me/favorites
     * Удалить релизы из избранного
     * Удаляет релизы из избранного авторизованного пользователя
     * Responses:
     *  - 200: Релизы успешно удалены
     *  - 403: Необходимо авторизоваться
     *
     * @param accountsUsersMeFavoritesPostRequestInner 
     * @return [kotlin.collections.List<kotlin.Int>]
     */
    @DELETE("accounts/users/me/favorites")
    suspend fun accountsUsersMeFavoritesDelete(@Body accountsUsersMeFavoritesPostRequestInner: kotlin.collections.List<AccountsUsersMeFavoritesPostRequestInner>): Response<kotlin.collections.List<kotlin.Int>>

    /**
     * GET accounts/users/me/favorites/ids
     * Список идентификаторов релизов добавленных в избранное
     * Возвращает данные по идентификаторам релизов из избранного авторизованного пользователя
     * Responses:
     *  - 200: Список релизов из избранного
     *  - 403: Необходимо авторизоваться
     *
     * @return [kotlin.collections.List<kotlin.Int>]
     */
    @GET("accounts/users/me/favorites/ids")
    suspend fun accountsUsersMeFavoritesIdsGet(): Response<kotlin.collections.List<kotlin.Int>>

    /**
     * POST accounts/users/me/favorites
     * Добавить релизы в избранное
     * Добавляет релизы в избранное авторизованного пользователя
     * Responses:
     *  - 200: Релизы успешно добавлены в избранное
     *  - 403: Необходимо авторизоваться
     *
     * @param accountsUsersMeFavoritesPostRequestInner 
     * @return [kotlin.collections.List<kotlin.Int>]
     */
    @POST("accounts/users/me/favorites")
    suspend fun accountsUsersMeFavoritesPost(@Body accountsUsersMeFavoritesPostRequestInner: kotlin.collections.List<AccountsUsersMeFavoritesPostRequestInner>): Response<kotlin.collections.List<kotlin.Int>>

    /**
     * GET accounts/users/me/favorites/references/age-ratings
     * Список возрастных рейтингов в избранном пользователя
     * Возвращает список возрастных рейтингов в избранном текущего пользователя
     * Responses:
     *  - 200: Список возрастных рейтингов
     *  - 403: Пользователь не авторизован
     *
     * @return [kotlin.collections.List<ResponsesApiV1AccountsUsersMeFavoritesReferencesAgeRatingsInner>]
     */
    @GET("accounts/users/me/favorites/references/age-ratings")
    suspend fun accountsUsersMeFavoritesReferencesAgeRatingsGet(): Response<kotlin.collections.List<ResponsesApiV1AccountsUsersMeFavoritesReferencesAgeRatingsInner>>

    /**
     * GET accounts/users/me/favorites/references/genres
     * Список жанров в избранном пользователя
     * Возвращает список жанров в избранном текущего пользователя
     * Responses:
     *  - 200: Список жанров
     *  - 403: Пользователь не авторизован
     *
     * @return [kotlin.collections.List<ResponsesApiV1AccountsUsersMeCollectionsReferencesGenresInner>]
     */
    @GET("accounts/users/me/favorites/references/genres")
    suspend fun accountsUsersMeFavoritesReferencesGenresGet(): Response<kotlin.collections.List<ResponsesApiV1AccountsUsersMeCollectionsReferencesGenresInner>>

    /**
     * GET accounts/users/me/favorites/references/sorting
     * Список опций сортировки в избранном пользователя
     * Возвращает список опций сортировки в избранном текущего пользователя
     * Responses:
     *  - 200: Список опций сортировки
     *  - 403: Пользователь не авторизован
     *
     * @return [kotlin.collections.List<ResponsesApiV1AccountsUsersMeFavoritesReferencesSortingInner>]
     */
    @GET("accounts/users/me/favorites/references/sorting")
    suspend fun accountsUsersMeFavoritesReferencesSortingGet(): Response<kotlin.collections.List<ResponsesApiV1AccountsUsersMeFavoritesReferencesSortingInner>>

    /**
     * GET accounts/users/me/favorites/references/types
     * Список типов релизов в избранном пользователя
     * Возвращает список типов релизов в избранном текущего пользователя
     * Responses:
     *  - 200: Список типов релизов
     *  - 403: Пользователь не авторизован
     *
     * @return [kotlin.collections.List<ResponsesApiV1AccountsUsersMeFavoritesReferencesTypesInner>]
     */
    @GET("accounts/users/me/favorites/references/types")
    suspend fun accountsUsersMeFavoritesReferencesTypesGet(): Response<kotlin.collections.List<ResponsesApiV1AccountsUsersMeFavoritesReferencesTypesInner>>

    /**
     * GET accounts/users/me/favorites/references/years
     * Список годов выхода релизов в избранном пользователя
     * Возвращает список годов выхода релизов в избранном текущего пользователя
     * Responses:
     *  - 200: Список годов выхода релизов
     *  - 403: Пользователь не авторизован
     *
     * @return [kotlin.collections.List<kotlin.Int>]
     */
    @GET("accounts/users/me/favorites/references/years")
    suspend fun accountsUsersMeFavoritesReferencesYearsGet(): Response<kotlin.collections.List<kotlin.Int>>

    /**
     * GET accounts/users/me/favorites/releases
     * Список релизов в избранном пользователя
     * Возвращает данные по релизам из избранного текущего пользователя
     * Responses:
     *  - 200: Список релизов
     *  - 403: Необходимо авторизоваться
     *  - 422: Ошибка валидации входных параметров
     *
     * @param page Номер страницы (optional)
     * @param limit Ограничение на количество элементов (optional)
     * @param fYears Года выхода релиза (optional)
     * @param fTypes Список типов релизов (optional)
     * @param fGenres Список идентификаторов жанров (optional)
     * @param fSearch Поисковый запрос (optional)
     * @param fSorting Тип сортировки (optional)
     * @param fAgeRatings Список возрастных рейтингов (optional)
     * @return [Responsesv1accountsusersmefavoritesreleases]
     */
    @GET("accounts/users/me/favorites/releases")
    suspend fun accountsUsersMeFavoritesReleasesGet(@Query("page") page: kotlin.Int? = null, @Query("limit") limit: kotlin.Int? = null, @Query("f[years]") fYears: kotlin.String? = null, @Query("f[types]") fTypes: CSVParams? = null, @Query("f[genres]") fGenres: kotlin.String? = null, @Query("f[search]") fSearch: kotlin.String? = null, @Query("f[sorting]") fSorting: Enumsaccountsusersuserfavoritefiltersorting? = null, @Query("f[age_ratings]") fAgeRatings: CSVParams? = null): Response<Responsesv1accountsusersmefavoritesreleases>

    /**
     * POST accounts/users/me/favorites/releases
     * Список релизов в избранном пользователя
     * Возвращает данные по релизам из избранного текущего пользователя
     * Responses:
     *  - 200: Список релизов
     *  - 403: Необходимо авторизоваться
     *  - 422: Ошибка валидации входных параметров
     *
     * @param accountsUsersMeFavoritesReleasesPostRequest 
     * @return [Responsesv1accountsusersmefavoritesreleases]
     */
    @POST("accounts/users/me/favorites/releases")
    suspend fun accountsUsersMeFavoritesReleasesPost(@Body accountsUsersMeFavoritesReleasesPostRequest: AccountsUsersMeFavoritesReleasesPostRequest): Response<Responsesv1accountsusersmefavoritesreleases>

    /**
     * GET accounts/users/me/profile
     * Профиль авторизованного пользователя
     * Возвращает данные профиля авторизованного пользователя
     * Responses:
     *  - 200: Данные профиля пользователя
     *  - 403: Необходимо авторизоваться
     *  - 404: Пользователь не авторизован
     *
     * @return [Modelsusersv1user]
     */
    @GET("accounts/users/me/profile")
    suspend fun accountsUsersMeProfileGet(): Response<Modelsusersv1user>

    /**
     * DELETE accounts/users/me/views/timecodes
     * Удаление таймкодов просмотра эпизодов
     * Удаляет данные по таймкодам просмотров для указанных эпизодов
     * Responses:
     *  - 200: Таймкоды просмотренных эпизодов удалены успешно
     *  - 403: Необходимо авторизоваться
     *
     * @param accountsUsersMeViewsTimecodesDeleteRequestInner 
     * @return [kotlin.collections.List<kotlin.collections.List<Responsesv1accountsusersmeviewstimecodesitem>>]
     */
    @DELETE("accounts/users/me/views/timecodes")
    suspend fun accountsUsersMeViewsTimecodesDelete(@Body accountsUsersMeViewsTimecodesDeleteRequestInner: kotlin.collections.List<AccountsUsersMeViewsTimecodesDeleteRequestInner>): Response<kotlin.collections.List<kotlin.collections.List<Responsesv1accountsusersmeviewstimecodesitem>>>

    /**
     * GET accounts/users/me/views/timecodes
     * Таймкоды просмотренных эпизодов
     * Возвращает таймкоды по прогрессу просмотренных эпизодов
     * Responses:
     *  - 200: Список таймкодов просмотренных эпизодов
     *  - 403: Необходимо авторизоваться
     *
     * @return [kotlin.collections.List<kotlin.collections.List<Responsesv1accountsusersmeviewstimecodesitem>>]
     */
    @GET("accounts/users/me/views/timecodes")
    suspend fun accountsUsersMeViewsTimecodesGet(): Response<kotlin.collections.List<kotlin.collections.List<Responsesv1accountsusersmeviewstimecodesitem>>>

    /**
     * POST accounts/users/me/views/timecodes
     * Обновление таймкодов прогресса просмотренного эпизода
     * Обновляет таймкоды просмотренных эпизодов
     * Responses:
     *  - 200: Таймкоды просмотренных эпизодов обновлены успешно
     *  - 403: Необходимо авторизоваться
     *
     * @param accountsUsersMeViewsTimecodesPostRequestInner 
     * @return [kotlin.collections.List<kotlin.collections.List<Responsesv1accountsusersmeviewstimecodesitem>>]
     */
    @POST("accounts/users/me/views/timecodes")
    suspend fun accountsUsersMeViewsTimecodesPost(@Body accountsUsersMeViewsTimecodesPostRequestInner: kotlin.collections.List<AccountsUsersMeViewsTimecodesPostRequestInner>): Response<kotlin.collections.List<kotlin.collections.List<Responsesv1accountsusersmeviewstimecodesitem>>>

    /**
     * GET anime/catalog/references/age-ratings
     * Список возрастных рейтингов в каталоге
     * Возвращает список возможных возрастных рейтингов в каталоге
     * Responses:
     *  - 200: Список возрастных рейтингов
     *
     * @return [kotlin.collections.List<ResponsesApiV1AnimeCatalogReferencesAgeRatingsInner>]
     */
    @GET("anime/catalog/references/age-ratings")
    suspend fun animeCatalogReferencesAgeRatingsGet(): Response<kotlin.collections.List<ResponsesApiV1AnimeCatalogReferencesAgeRatingsInner>>

    /**
     * GET anime/catalog/references/genres
     * Список жанров в каталоге
     * Возвращает список всех жанров в каталоге
     * Responses:
     *  - 200: Список жанров
     *
     * @return [kotlin.collections.List<ResponsesApiV1AnimeCatalogReferencesGenresInner>]
     */
    @GET("anime/catalog/references/genres")
    suspend fun animeCatalogReferencesGenresGet(): Response<kotlin.collections.List<ResponsesApiV1AnimeCatalogReferencesGenresInner>>

    /**
     * GET anime/catalog/references/production-statuses
     * Список возможных статусов озвучки релиза в каталоге
     * Возвращает список возможных статусов озвучки релиза в каталоге
     * Responses:
     *  - 200: Список статусов
     *
     * @return [kotlin.collections.List<ResponsesApiV1AnimeCatalogReferencesProductionStatusesInner>]
     */
    @GET("anime/catalog/references/production-statuses")
    suspend fun animeCatalogReferencesProductionStatusesGet(): Response<kotlin.collections.List<ResponsesApiV1AnimeCatalogReferencesProductionStatusesInner>>

    /**
     * GET anime/catalog/references/publish-statuses
     * Список возможных статусов выхода релиза в каталоге
     * Возвращает список возможных статусов выхода релиза в каталоге
     * Responses:
     *  - 200: Список статусов
     *
     * @return [kotlin.collections.List<ResponsesApiV1AnimeCatalogReferencesPublishStatusesInner>]
     */
    @GET("anime/catalog/references/publish-statuses")
    suspend fun animeCatalogReferencesPublishStatusesGet(): Response<kotlin.collections.List<ResponsesApiV1AnimeCatalogReferencesPublishStatusesInner>>

    /**
     * GET anime/catalog/references/seasons
     * Список сезонов релиза в каталоге
     * Возвращает список возможных сезонов релизов в каталоге
     * Responses:
     *  - 200: Список сезонов релизов
     *
     * @return [kotlin.collections.List<ResponsesV1AnimeCatalogReferencesSeasonsInner>]
     */
    @GET("anime/catalog/references/seasons")
    suspend fun animeCatalogReferencesSeasonsGet(): Response<kotlin.collections.List<ResponsesV1AnimeCatalogReferencesSeasonsInner>>

    /**
     * GET anime/catalog/references/sorting
     * Список возможных типов сортировок в каталоге
     * Возвращает список возможных типов сортировок в каталоге
     * Responses:
     *  - 200: Список типов сортировок
     *
     * @return [kotlin.collections.List<ResponsesV1AnimeCatalogReferencesSortingInner>]
     */
    @GET("anime/catalog/references/sorting")
    suspend fun animeCatalogReferencesSortingGet(): Response<kotlin.collections.List<ResponsesV1AnimeCatalogReferencesSortingInner>>

    /**
     * GET anime/catalog/references/types
     * Список типов релизов в каталоге
     * Возвращает список возможных типов релизов в каталоге
     * Responses:
     *  - 200: Список типов релизов
     *
     * @return [kotlin.collections.List<ResponsesV1AnimeCatalogReferencesTypesInner>]
     */
    @GET("anime/catalog/references/types")
    suspend fun animeCatalogReferencesTypesGet(): Response<kotlin.collections.List<ResponsesV1AnimeCatalogReferencesTypesInner>>

    /**
     * GET anime/catalog/references/years
     * Список годов в каталоге
     * Возвращает список годов в каталоге
     * Responses:
     *  - 200: Список годов
     *
     * @return [kotlin.collections.List<kotlin.Int>]
     */
    @GET("anime/catalog/references/years")
    suspend fun animeCatalogReferencesYearsGet(): Response<kotlin.collections.List<kotlin.Int>>

    /**
     * GET anime/catalog/releases
     * Список релизов в каталоге
     * Возвращает список релизов по заданными параметрам
     * Responses:
     *  - 200: Список релизов
     *  - 422: Ошибка валидации входных параметров
     *
     * @param page Номер страницы (optional)
     * @param limit Ограничение на количество элементов (optional)
     * @param fGenres Список идентификаторов жанров (optional)
     * @param fTypes Список типов релизов (optional)
     * @param fSeasons Список сезонов релизов (optional)
     * @param fYearsFromYear Минимальный год выхода релиза (optional)
     * @param fYearsToYear Максимальный год выхода релиза (optional)
     * @param fSearch Поиск запрос (optional)
     * @param fSorting Тип сортировки (optional)
     * @param fAgeRatings Список возрастных рейтингов (optional)
     * @param fPublishStatuses Список статусов релизов (optional)
     * @param fProductionStatuses Список статусов релизов (optional)
     * @return [Responsesapiv1animecatalogreleases]
     */
    @GET("anime/catalog/releases")
    suspend fun animeCatalogReleasesGet(@Query("page") page: kotlin.Int? = null, @Query("limit") limit: kotlin.Int? = null, @Query("f[genres]") fGenres: kotlin.String? = null, @Query("f[types]") fTypes: CSVParams? = null, @Query("f[seasons]") fSeasons: CSVParams? = null, @Query("f[years][from_year]") fYearsFromYear: kotlin.Int? = null, @Query("f[years][to_year]") fYearsToYear: kotlin.Int? = null, @Query("f[search]") fSearch: kotlin.String? = null, @Query("f[sorting]") fSorting: Enumsanimecatalogfiltersorting? = null, @Query("f[age_ratings]") fAgeRatings: CSVParams? = null, @Query("f[publish_statuses]") fPublishStatuses: CSVParams? = null, @Query("f[production_statuses]") fProductionStatuses: CSVParams? = null): Response<Responsesapiv1animecatalogreleases>

    /**
     * POST anime/catalog/releases
     * Список релизов в каталоге
     * Возвращает список релизов по заданными параметрам
     * Responses:
     *  - 200: Список релизов
     *  - 422: Ошибка валидации входных параметров
     *
     * @param animeCatalogReleasesPostRequest 
     * @return [Responsesapiv1animecatalogreleases]
     */
    @POST("anime/catalog/releases")
    suspend fun animeCatalogReleasesPost(@Body animeCatalogReleasesPostRequest: AnimeCatalogReleasesPostRequest): Response<Responsesapiv1animecatalogreleases>

    /**
     * GET anime/franchises/{franchiseId}
     * Получить франшизу
     * Возвращает данные франшизы
     * Responses:
     *  - 200: Данные франшизы
     *
     * @param franchiseId ID франшизы
     * @return [Responsesv1animefranchise]
     */
    @GET("anime/franchises/{franchiseId}")
    suspend fun animeFranchisesFranchiseIdGet(@Path("franchiseId") franchiseId: kotlin.String): Response<Responsesv1animefranchise>

    /**
     * GET anime/franchises
     * Получить список франшиз
     * Возвращает список франшиз.
     * Responses:
     *  - 200: Список франшиз
     *
     * @return [Responsesv1animefranchises]
     */
    @GET("anime/franchises")
    suspend fun animeFranchisesGet(): Response<Responsesv1animefranchises>

    /**
     * GET anime/franchises/random
     * Получить список случайных франшиз
     * Возвращает список случайных франшиз.
     * Responses:
     *  - 200: Список франшиз
     *
     * @param limit Количество случайных франшиз в выдаче (optional)
     * @return [Responsesv1animefranchisesrandom]
     */
    @GET("anime/franchises/random")
    suspend fun animeFranchisesRandomGet(@Query("limit") limit: kotlin.Int? = null): Response<Responsesv1animefranchisesrandom>

    /**
     * GET anime/franchises/release/{releaseId}
     * Получить список франшиз для релиза
     * Возвращает список франшиз, в которых участвует релиз
     * Responses:
     *  - 200: Список франшиз релиза
     *
     * @param releaseId ID релиза
     * @return [Responsesv1animefranchisesbyRelease]
     */
    @GET("anime/franchises/release/{releaseId}")
    suspend fun animeFranchisesReleaseReleaseIdGet(@Path("releaseId") releaseId: kotlin.String): Response<Responsesv1animefranchisesbyRelease>

    /**
     * GET anime/genres/{genreId}
     * Данные по жанру
     * Возвращает данные по жанру
     * Responses:
     *  - 200: Данные по жанру
     *
     * @param genreId ID Жанра
     * @return [Responsesapiv1animegenresitem]
     */
    @GET("anime/genres/{genreId}")
    suspend fun animeGenresGenreIdGet(@Path("genreId") genreId: kotlin.Int): Response<Responsesapiv1animegenresitem>

    /**
     * GET anime/genres/{genreId}/releases
     * Список релизов жанра
     * Возвращает список всех релизов жанра
     * Responses:
     *  - 200: Список релизов жанра
     *
     * @param genreId ID Жанра
     * @param page Номер страницы (optional)
     * @param limit Ограничение на количество элементов (optional)
     * @return [Responsesapiv1animegenresreleases]
     */
    @GET("anime/genres/{genreId}/releases")
    suspend fun animeGenresGenreIdReleasesGet(@Path("genreId") genreId: kotlin.Int, @Query("page") page: kotlin.Int? = null, @Query("limit") limit: kotlin.Int? = null): Response<Responsesapiv1animegenresreleases>

    /**
     * GET anime/genres
     * Список всех жанров
     * Возвращает список всех жанров
     * Responses:
     *  - 200: Список доступных жанров
     *
     * @return [kotlin.collections.List<Modelsanimegenresv1genre>]
     */
    @GET("anime/genres")
    suspend fun animeGenresGet(): Response<kotlin.collections.List<Modelsanimegenresv1genre>>

    /**
     * GET anime/genres/random
     * Список случайных жанров
     * Возвращает список случайных жанров
     * Responses:
     *  - 200: Список доступных жанров
     *
     * @param limit Количество жанров в выдаче (optional)
     * @return [kotlin.collections.List<Modelsanimegenresv1genre>]
     */
    @GET("anime/genres/random")
    suspend fun animeGenresRandomGet(@Query("limit") limit: kotlin.Int? = null): Response<kotlin.collections.List<Modelsanimegenresv1genre>>

    /**
     * GET anime/releases/{aliasOrId}
     * Данные по релизу
     * Возвращает данные по релизу
     * Responses:
     *  - 200: Данные по релизу
     *
     * @param aliasOrId Alias или Id релиза
     * @return [kotlin.collections.List<ResponsesApiV1AnimeReleasesReleaseInner>]
     */
    @GET("anime/releases/{aliasOrId}")
    suspend fun animeReleasesAliasOrIdGet(@Path("aliasOrId") aliasOrId: kotlin.String): Response<kotlin.collections.List<ResponsesApiV1AnimeReleasesReleaseInner>>

    /**
     * GET anime/releases/{aliasOrId}/members
     * Список участников, которые работали над релизом
     * Возвращает данные по участникам релиза
     * Responses:
     *  - 200: Данные по участникам в релизе
     *
     * @param aliasOrId Alias или Id релиза
     * @return [kotlin.collections.List<Modelsanimereleasesv1releasemember>]
     */
    @GET("anime/releases/{aliasOrId}/members")
    suspend fun animeReleasesAliasOrIdMembersGet(@Path("aliasOrId") aliasOrId: kotlin.String): Response<kotlin.collections.List<Modelsanimereleasesv1releasemember>>

    /**
     * GET anime/releases/episodes/{releaseEpisodeId}
     * Данные по эпизоду
     * Возвращает данные по эпизоду
     * Responses:
     *  - 200: Данные по эпизоду
     *  - 404: Эпизод не найден
     *
     * @param releaseEpisodeId Идентификатор эпизода
     * @return [Responsesapiv1animereleasesepisode]
     */
    @GET("anime/releases/episodes/{releaseEpisodeId}")
    suspend fun animeReleasesEpisodesReleaseEpisodeIdGet(@Path("releaseEpisodeId") releaseEpisodeId: kotlin.String): Response<Responsesapiv1animereleasesepisode>

    /**
     * GET anime/releases/latest
     * Последние релизы
     * Возвращает данные по последним релизам
     * Responses:
     *  - 200: Данные по релизам
     *  - 422: Ошибка валидации входных параметров
     *
     * @param limit Количество последних релизов в выдаче (optional)
     * @return [kotlin.collections.List<ResponsesApiV1AnimeReleasesLatestInner>]
     */
    @GET("anime/releases/latest")
    suspend fun animeReleasesLatestGet(@Query("limit") limit: kotlin.Int? = null): Response<kotlin.collections.List<ResponsesApiV1AnimeReleasesLatestInner>>

    /**
     * GET anime/releases/list
     * Данные по списку релизов
     * Возвращает данные по списку релизов
     * Responses:
     *  - 200: Данные по релизу
     *  - 422: Ошибка валидации входных параметров
     *
     * @param ids Список ID релизов (optional)
     * @param aliases Список alias релизов (optional)
     * @param page Номер страницы (optional)
     * @param limit Ограничение на количество элементов (optional)
     * @return [Responsesapiv1animereleaseslist]
     */
    @GET("anime/releases/list")
    suspend fun animeReleasesListGet(@Query("ids") ids: CSVParams? = null, @Query("aliases") aliases: CSVParams? = null, @Query("page") page: kotlin.Int? = null, @Query("limit") limit: kotlin.Int? = null): Response<Responsesapiv1animereleaseslist>

    /**
     * GET anime/releases/random
     * Данные по случайным релизам
     * Возвращает данные по случайным релизам
     * Responses:
     *  - 200: Данные по релизам
     *
     * @param limit Количество случайных релизов (optional)
     * @return [kotlin.collections.List<Modelsanimereleasesv1release>]
     */
    @GET("anime/releases/random")
    suspend fun animeReleasesRandomGet(@Query("limit") limit: kotlin.Int? = null): Response<kotlin.collections.List<Modelsanimereleasesv1release>>

    /**
     * GET anime/schedule/now
     * Данные по расписанию релизов на текущую дату
     * Возвращает список релизов в расписании на текущую дату
     * Responses:
     *  - 200: Данные по релизам в расписании
     *
     * @return [Responsesv1animeschedulenow]
     */
    @GET("anime/schedule/now")
    suspend fun animeScheduleNowGet(): Response<Responsesv1animeschedulenow>

    /**
     * GET anime/schedule/week
     * Данные по расписанию релизов на текущую неделю
     * Возвращает список релизов в расписании на текущую неделю
     * Responses:
     *  - 200: Данные по релизам в расписании
     *
     * @return [Responsesv1animescheduleweek]
     */
    @GET("anime/schedule/week")
    suspend fun animeScheduleWeekGet(): Response<Responsesv1animescheduleweek>

    /**
     * GET anime/torrents
     * Данные по торрентам
     * Возвращает данные по последним торрентам
     * Responses:
     *  - 200: Данные по торренту
     *  - 422: Ошибка валидации входных параметров
     *
     * @param page Номер страницы (optional)
     * @param limit Ограничение на количество элементов (optional)
     * @return [Responsesapiv1animetorrents]
     */
    @GET("anime/torrents")
    suspend fun animeTorrentsGet(@Query("page") page: kotlin.Int? = null, @Query("limit") limit: kotlin.Int? = null): Response<Responsesapiv1animetorrents>

    /**
     * GET anime/torrents/{hashOrId}/file
     * Торрент-файл по его hash или id
     * Возвращает торрент-файл
     * Responses:
     *  - 200: Торрент файл
     *  - 404: Не удалось найти торрент по такому hash значению
     *
     * @param hashOrId Hash или ID торрента
     * @param pk passkey пользователя. Оставьте пустым для собственного pk (если аутентифицирован) (optional)
     * @return [ResponseBody]
     */
    @GET("anime/torrents/{hashOrId}/file")
    suspend fun animeTorrentsHashOrIdFileGet(@Path("hashOrId") hashOrId: kotlin.String, @Query("pk") pk: kotlin.String? = null): Response<ResponseBody>

    /**
     * GET anime/torrents/{hashOrId}
     * Данные по торренту
     * Возвращает данные по торренту
     * Responses:
     *  - 200: Данные по торренту
     *  - 404: Не удалось найти торрент по такому hash значению
     *
     * @param hashOrId Hash или ID торрента
     * @return [Responsesapiv1animetorrent]
     */
    @GET("anime/torrents/{hashOrId}")
    suspend fun animeTorrentsHashOrIdGet(@Path("hashOrId") hashOrId: kotlin.String): Response<Responsesapiv1animetorrent>

    /**
     * GET anime/torrents/release/{releaseId}
     * Данные по торрентам для релиза
     * Возвращает данные по торрентам релиза
     * Responses:
     *  - 200: Данные по торрентам
     *  - 404: Не удалось найти релиз с таким ID
     *
     * @param releaseId ID релиза
     * @return [kotlin.collections.List<ResponsesApiV1AnimeTorrentsReleaseTorrentsInner>]
     */
    @GET("anime/torrents/release/{releaseId}")
    suspend fun animeTorrentsReleaseReleaseIdGet(@Path("releaseId") releaseId: kotlin.Int): Response<kotlin.collections.List<ResponsesApiV1AnimeTorrentsReleaseTorrentsInner>>

    /**
     * GET anime/torrents/rss
     * RSS лента последних торрентов
     * Возвращает данные по последним торрентам в виде XML документа
     * Responses:
     *  - 200: XML документ
     *
     * @param limit Количество торрентов в выдаче. По умолчанию 10 (optional)
     * @param pk Пользовательский passkey. Персонализирует ссылки на торренты для учета статистики (optional)
     * @return [Unit]
     */
    @GET("anime/torrents/rss")
    suspend fun animeTorrentsRssGet(@Query("limit") limit: kotlin.Int? = null, @Query("pk") pk: kotlin.String? = null): Response<Unit>

    /**
     * GET anime/torrents/rss/release/{releaseId}
     * RSS лента торрентов релиза
     * Возвращает данные по торрентам релиза в виде RSS ленты
     * Responses:
     *  - 200: XML документ
     *
     * @param releaseId ID релиза
     * @param pk Пользовательский passkey. Персонализирует ссылки на торренты для учета статистики (optional)
     * @return [Unit]
     */
    @GET("anime/torrents/rss/release/{releaseId}")
    suspend fun animeTorrentsRssReleaseReleaseIdGet(@Path("releaseId") releaseId: kotlin.Int, @Query("pk") pk: kotlin.String? = null): Response<Unit>

    /**
     * GET app/search/releases
     * Поиск релизов
     * Возвращает данные по релизам, которые удовлетворяют поисковому запросу
     * Responses:
     *  - 200: Найденные релизы
     *
     * @param query Поисковая строка
     * @return [kotlin.collections.List<Modelsanimereleasesv1release>]
     */
    @GET("app/search/releases")
    suspend fun appSearchReleasesGet(@Query("query") query: kotlin.String): Response<kotlin.collections.List<Modelsanimereleasesv1release>>

    /**
     * GET media/promotions
     * Список промо-материалов
     * Возвращает список промо-материалов или рекламные кампании в случайном порядке
     * Responses:
     *  - 200: Промо-материал
     *
     * @return [Responsesv1mediapromotions]
     */
    @GET("media/promotions")
    suspend fun mediaPromotionsGet(): Response<Responsesv1mediapromotions>

    /**
     * GET media/videos
     * Список видео-роликов
     * Возвращает список последних видео-роликов
     * Responses:
     *  - 200: Список видео-роликов
     *
     * @param limit Количество роликов в выдаче (optional)
     * @return [Responsesv1mediavideos]
     */
    @GET("media/videos")
    suspend fun mediaVideosGet(@Query("limit") limit: kotlin.Int? = null): Response<Responsesv1mediavideos>

    /**
     * GET teams/
     * Список команд АниЛибрии
     * Возвращает список всех команд
     * Responses:
     *  - 200: Команды
     *
     * @return [kotlin.collections.List<Modelsteamsv1team>]
     */
    @GET("teams/")
    suspend fun teamsGet(): Response<kotlin.collections.List<Modelsteamsv1team>>

    /**
     * GET teams/roles
     * Список ролей
     * Возвращает список всех ролей в командах
     * Responses:
     *  - 200: Роли в командах
     *
     * @return [kotlin.collections.List<Modelsteamsv1teamrole>]
     */
    @GET("teams/roles")
    suspend fun teamsRolesGet(): Response<kotlin.collections.List<Modelsteamsv1teamrole>>

    /**
     * GET teams/users
     * Список анилибрийцов
     * Возвращает список всех анилибрийцов с указанием команды и своих ролей
     * Responses:
     *  - 200: Анилибрийцы
     *
     * @return [kotlin.collections.List<ResponsesApiV1TeamsUsersInner>]
     */
    @GET("teams/users")
    suspend fun teamsUsersGet(): Response<kotlin.collections.List<ResponsesApiV1TeamsUsersInner>>

}
