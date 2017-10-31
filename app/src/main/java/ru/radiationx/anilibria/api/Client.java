package ru.radiationx.anilibria.api;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by radiationx on 31.10.17.
 */

public class Client {
    private static Client INSTANCE = null;
    private final OkHttpClient client = new OkHttpClient();

    public static Client get() {
        if (INSTANCE == null) INSTANCE = new Client();
        return INSTANCE;
    }


    public String get(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response okHttpResponse = null;
        String responseText = null;
        try {
            okHttpResponse = client.newCall(request).execute();
            if (!okHttpResponse.isSuccessful())
                throw new IOException("Unexpected code " + okHttpResponse);

            ResponseBody responseBody = okHttpResponse.body();
            responseText = responseBody.string();
        } finally {
            if (okHttpResponse != null)
                okHttpResponse.close();
        }

        return responseText;
    }

    public void get(String url, final ClientCallback clientCallback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                clientCallback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    clientCallback.onError(new IOException("Unexpected code " + response));
                    return;
                }
                ResponseBody responseBody = response.body();
                try {
                    if (responseBody != null) {
                        clientCallback.onResponse(responseBody.string());
                    } else {
                        clientCallback.onResponse(null);
                    }
                } catch (Exception ex) {
                    clientCallback.onError(ex);
                }
            }
        });
    }

    public interface ClientCallback {
        void onError(Exception e);

        void onResponse(String responseText) throws Exception;
    }
}
