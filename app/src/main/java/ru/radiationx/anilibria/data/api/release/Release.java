package ru.radiationx.anilibria.data.api.release;

import android.text.Html;

import org.json.JSONArray;
import org.json.JSONObject;

import io.reactivex.Observable;
import ru.radiationx.anilibria.data.Client;

/**
 * Created by radiationx on 05.11.17.
 */

public class Release {

    public Observable<FullRelease> getRelease(int id) {
        return Observable.fromCallable(() -> {
            String url = "http://www.anilibria.tv/api/api.php?action=release&ELEMENT_ID=" + id;
            String response = Client.get().get(url);
            return parseRelease(response);
        });
    }

    private FullRelease parseRelease(String responseText) throws Exception {
        FullRelease release = new FullRelease();

        final JSONObject responseJson = new JSONObject(responseText);
        //item.setId(responseJson.getInt("id"));

        String[] titles = responseJson.getString("title").split(" / ");
        if (titles.length > 0) {
            release.setOriginalTitle(Html.fromHtml(titles[0]).toString());
            if (titles.length > 1) {
                release.setTitle(Html.fromHtml(titles[1]).toString());
            }
        }

        release.setTorrentLink(responseJson.getString("torrent_link"));
        //item.setLink(responseJson.getString("link"));
        release.setImage(responseJson.getString("image"));
        //release.setEpisodes(responseJson.getString("episode"));
        release.setDescription(Html.fromHtml(responseJson.getString("description")).toString());

        JSONArray jsonSeasons = responseJson.getJSONArray("season");
        for (int j = 0; j < jsonSeasons.length(); j++) {
            release.addSeason(jsonSeasons.getString(j));
        }

        JSONArray jsonVoices = responseJson.getJSONArray("voices");
        for (int j = 0; j < jsonVoices.length(); j++) {
            release.addVoice(jsonVoices.getString(j));
        }

        JSONArray jsonGenres = responseJson.getJSONArray("genres");
        for (int j = 0; j < jsonGenres.length(); j++) {
            release.addGenre(jsonGenres.getString(j));
        }

        JSONArray jsonTypes = responseJson.getJSONArray("types");
        for (int j = 0; j < jsonTypes.length(); j++) {
            release.addType(jsonTypes.getString(j));
        }

        return release;
    }
}
