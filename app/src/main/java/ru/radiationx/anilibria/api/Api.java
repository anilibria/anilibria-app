package ru.radiationx.anilibria.api;

/**
 * Created by radiationx on 31.10.17.
 */

public class Api {
    public interface ApiCallback<T> {
        void onError(Exception e);

        void onResponse(T object) throws Exception;
    }
}
