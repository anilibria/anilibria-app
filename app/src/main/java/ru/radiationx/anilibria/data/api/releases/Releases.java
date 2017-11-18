package ru.radiationx.anilibria.data.api.releases;

import android.text.Html;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.Observable;
import ru.radiationx.anilibria.data.Client;
import ru.radiationx.anilibria.data.api.Api;

/**
 * Created by radiationx on 31.10.17.
 */

public class Releases {

    public Observable<ArrayList<ReleaseItem>> getItems(int page) {
        return Observable.fromCallable(() -> {
            String url = "https://www.anilibria.tv/api/api.php?PAGEN_1=" + page;
            String response = Client.get().get(url);
            return parseItems(response);
        });
    }

    private ArrayList<ReleaseItem> parseItems(String responseText) throws Exception {
        ArrayList<ReleaseItem> resItems = new ArrayList<>();
        final JSONObject responseJson = new JSONObject(responseText);
        final JSONArray jsonItems = responseJson.getJSONArray("items");
        for (int i = 0; i < jsonItems.length(); i++) {
            ReleaseItem item = new ReleaseItem();
            JSONObject jsonItem = jsonItems.getJSONObject(i);
            item.setId(jsonItem.getInt("id"));

            String[] titles = jsonItem.getString("title").split(" / ");
            if (titles.length > 0) {
                item.setOriginalTitle(Html.fromHtml(titles[0]).toString());
                if (titles.length > 1) {
                    item.setTitle(Html.fromHtml(titles[1]).toString());
                }
            }

            item.setTorrentLink(Api.BASE_URL + jsonItem.getString("torrent_link"));
            item.setLink(Api.BASE_URL + jsonItem.getString("link"));
            item.setImage(Api.BASE_URL + jsonItem.getString("image"));
            item.setEpisodes(jsonItem.getString("episode"));
            item.setDescription(Html.fromHtml(jsonItem.getString("description")).toString());

            JSONArray jsonSeasons = jsonItem.getJSONArray("season");
            for (int j = 0; j < jsonSeasons.length(); j++) {
                item.addSeason(jsonSeasons.getString(j));
            }

            JSONArray jsonVoices = jsonItem.getJSONArray("voices");
            for (int j = 0; j < jsonVoices.length(); j++) {
                item.addVoice(jsonVoices.getString(j));
            }

            JSONArray jsonGenres = jsonItem.getJSONArray("genres");
            for (int j = 0; j < jsonGenres.length(); j++) {
                item.addGenre(jsonGenres.getString(j));
            }

            JSONArray jsonTypes = jsonItem.getJSONArray("types");
            for (int j = 0; j < jsonTypes.length(); j++) {
                item.addType(jsonTypes.getString(j));
            }

            resItems.add(item);
        }
        return resItems;
    }
}
