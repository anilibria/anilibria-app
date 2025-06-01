package ru.radiationx.data

import javax.inject.Qualifier

/* Preferences*/
@Qualifier
annotation class DataPreferences

/* OkHttpClient */
@Qualifier
annotation class ApiClient

@Qualifier
annotation class DirectClient

@Qualifier
annotation class PlayerClient

/* Retrofit */
@Qualifier
annotation class ApiRetrofit

@Qualifier
annotation class DirectRetrofit
