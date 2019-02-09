package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.ApiResponse
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.CommentParser
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser
import javax.inject.Inject

class CommentApi @Inject constructor(
        private val client: IClient,
        private val commentParser: CommentParser
) {

    fun getComments(id: Int, page: Int): Single<Paginated<List<Comment>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "comments",
                "id" to id.toString(),
                "from" to "release",
                "PAGEN_1" to page.toString()
        )
        return client.post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { commentParser.comments(it) }
    }

    fun sendComment(code: String, id: Int, text: String, sessId: String): Single<Paginated<List<Comment>>> {
        val args = mapOf(
                //"index" to "ZZtH",
                "back_page" to "/api/api_v2.php?action=comments&id=$id&from=release&PAGEN_1=1",
                "ELEMENT_ID" to "$id",
                "SECTION_ID" to "",
                "save_product_review" to "Y",
                "preview_comment" to "N",
                "sessid" to sessId,
                //"autosave_id" to "27f64ba01ec10848c3c2cec1e137d06ac",
                "REVIEW_TEXT" to text,
                "REVIEW_USE_SMILES" to "Y"
        )

        return client.post("${Api.BASE_URL}/release/$code.html?ELEMENT_CODE=$code", args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { commentParser.comments(it) }
    }

    fun getCommentsArticle(id: Int, page: Int): Single<Paginated<List<Comment>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "comments",
                "id" to id.toString(),
                "from" to "article",
                "PAGEN_1" to page.toString()
        )
        return client
                .post(Api.API_URL, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { commentParser.comments(it) }
    }

    fun sendCommentArticle(url: String, id: Int, text: String, sessId: String): Single<Paginated<List<Comment>>> {
        val args = mapOf(
                //"index" to "ZZtH",
                "back_page" to "/api/api_v2.php?action=comments&id=$id&from=article&PAGEN_1=1",
                "ELEMENT_ID" to "$id",
                "SECTION_ID" to "",
                "save_product_review" to "Y",
                "preview_comment" to "N",
                "sessid" to sessId,
                //"autosave_id" to "27f64ba01ec10848c3c2cec1e137d06ac",
                "REVIEW_TEXT" to text,
                "REVIEW_USE_SMILES" to "Y"
        )

        return client.post(url, args)
                .compose(ApiResponse.fetchResult<JSONObject>())
                .map { commentParser.comments(it) }
    }
}