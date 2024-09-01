package anilibria.api.videos

import anilibria.api.videos.models.VideoResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

interface VideosApi {

    @GET("/media/videos")
    suspend fun getVideos(@Query("limit") limit: Int?): List<VideoResponse>
}