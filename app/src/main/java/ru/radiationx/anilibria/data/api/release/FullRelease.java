package ru.radiationx.anilibria.data.api.release;

import java.util.ArrayList;
import java.util.List;

import ru.radiationx.anilibria.data.api.releases.ReleaseItem;

/**
 * Created by radiationx on 05.11.17.
 */

public class FullRelease extends ReleaseItem {
    private List<Episode> episodes = new ArrayList<>();

    public List<Episode> getEpisodes() {
        return episodes;
    }
    public void addEpisode(Episode episode){
        episodes.add(episode);
    }

    public static class Episode {
        private String title;
        private String urlSd;
        private String urlHd;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrlSd() {
            return urlSd;
        }

        public void setUrlSd(String urlSd) {
            this.urlSd = urlSd;
        }

        public String getUrlHd() {
            return urlHd;
        }

        public void setUrlHd(String urlHd) {
            this.urlHd = urlHd;
        }
    }
}
