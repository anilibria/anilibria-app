package ru.radiationx.anilibria.data.api;

import ru.radiationx.anilibria.data.api.releases.Releases;

/**
 * Created by radiationx on 31.10.17.
 */

public class Api {
    private static Api instance = null;
    private Releases releases = null;

    public Releases Releases() {
        if (releases == null) {
            releases = new Releases();
        }
        return releases;
    }

    public static synchronized Api get() {
        if (instance == null) {
            instance = new Api();
        }
        return instance;
    }
}
