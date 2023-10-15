package ru.radiationx.data.system

class LocaleHolder {
    companion object {
        private val AVAIL_COUNTRIES =
            arrayOf("AZ", "AM", "BY", "KZ", "KG", "MD", "RU", "TJ", "UZ", "UA")

        fun checkAvail(countryCode: String): Boolean =
            AVAIL_COUNTRIES.find { it.equals(countryCode, true) } != null
    }
}