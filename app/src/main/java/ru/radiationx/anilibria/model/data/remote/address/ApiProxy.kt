package ru.radiationx.anilibria.model.data.remote.address

data class ApiProxy(
        val tag: String,
        val name: String?,
        val desc: String?,
        val ip: String,
        val port: Int,
        val user: String?,
        val password: String?
)