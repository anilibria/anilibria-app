package ru.radiationx.anilibria.api.releases;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.radiationx.anilibria.api.Api;
import ru.radiationx.anilibria.api.Client;

/**
 * Created by radiationx on 31.10.17.
 */

public class ReleaseParser {

    private static ArrayList<ReleaseItem> parseItems(String responseText) throws Exception {
        ArrayList<ReleaseItem> resItems = new ArrayList<>();
        final JSONObject responseJson = new JSONObject(responseText);
        final JSONArray jsonItems = responseJson.getJSONArray("items");
        for (int i = 0; i < jsonItems.length(); i++) {
            ReleaseItem item = new ReleaseItem();
            JSONObject jsonItem = jsonItems.getJSONObject(i);
            item.setId(jsonItem.getInt("id"));
            {
                String title = jsonItem.getString("title");
                String[] titles = title.split(" / ");
                for (int j = 0; j < titles.length; j++) {
                    title = titles[j];
                }
                item.setTitle(title);
            }

            item.setTorrentLink(jsonItem.getString("torrent_link"));
            item.setLink(jsonItem.getString("link"));
            item.setImage(jsonItem.getString("image"));
            item.setEpisodes(jsonItem.getString("episode"));
            item.setDescription(jsonItem.getString("description"));

            JSONArray jsonSeasons = jsonItem.getJSONArray("season");
            for (int j = 0; j < jsonSeasons.length(); j++) {
                item.addSeason(jsonSeasons.getString(j));
            }

            JSONArray jsonVoices = jsonItem.getJSONArray("season");
            for (int j = 0; j < jsonVoices.length(); j++) {
                item.addVoice(jsonVoices.getString(j));
            }

            JSONArray jsonGenres = jsonItem.getJSONArray("season");
            for (int j = 0; j < jsonGenres.length(); j++) {
                item.addGenre(jsonGenres.getString(j));
            }

            JSONArray jsonTypes = jsonItem.getJSONArray("season");
            for (int j = 0; j < jsonTypes.length(); j++) {
                item.addType(jsonTypes.getString(j));
            }

            resItems.add(item);
        }
        return resItems;
    }

    public static void releaseItemsAsync(int page, final Api.ApiCallback<ArrayList<ReleaseItem>> callback) {
        String url = "http://www.anilibria.tv/api/api.php?PAGEN_1=" + page;
        Client.get().get(url, new Client.ClientCallback() {
            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(String responseText) throws Exception {
                ArrayList<ReleaseItem> resItems = parseItems(responseText);
                callback.onResponse(resItems);
            }
        });
    }
}
