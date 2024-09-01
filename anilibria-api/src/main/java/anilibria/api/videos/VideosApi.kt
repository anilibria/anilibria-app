package anilibria.api.videos

import anilibria.api.videos.models.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface VideosApi {

    @GET("/media/videos")
    suspend fun getVideos(@Query("limit") limit: Int?): List<VideoResponse>
}