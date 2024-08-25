package anilibria.api

import de.jensklingenberg.ktorfit.http.GET

interface Api {

    @GET("")
    suspend fun kek(): Unit
}