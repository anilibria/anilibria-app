package ru.radiationx.anilibria.data.api;

import ru.radiationx.anilibria.data.api.release.Release;
import ru.radiationx.anilibria.data.api.releases.Releases;

/**
 * Created by radiationx on 31.10.17.
 */

public class Api {
    private static Api instance = null;
    private Releases releases = null;
    private Release release = null;

    public Releases Releases() {
        if (releases == null) {
            releases = new Releases();
        }
        return releases;
    }

    public Release Release() {
        if (release == null) {
            release = new Release();
        }
        return release;
    }


    public static synchronized Api get() {
        if (instance == null) {
            instance = new Api();
        }
        return instance;
    }
}
