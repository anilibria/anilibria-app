package ru.radiationx.anilibria.ui.fragments;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.radiationx.anilibria.App;
import ru.radiationx.anilibria.model.data.remote.IClient;

/**
 * Created by radiationx on 09.11.17.
 */

public class GoogleCaptchaActivity extends FragmentActivity {
    private WebView webView;
    private String content = "";
    private String url = "";

    private IClient client = App.injections.getClient();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            content = getIntent().getStringExtra("content");
            url = getIntent().getStringExtra("url");
        }

        webView = new WebView(this);
        setContentView(webView);
        webView.setWebViewClient(new CaptchaWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        Uri uri = Uri.parse(url);
        String domain = uri.getScheme() + "://" + uri.getHost();
        Log.e("GoogleCaptchaActivity", "domain: " + domain);
        webView.loadDataWithBaseURL(domain, content, "text/html", "utf-8", null);

    }

    class CaptchaWebViewClient extends WebViewClient {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Log.e("GoogleCaptchaActivity", "shouldInterceptRequest 21: "+request.getUrl());
            return super.shouldInterceptRequest(view, request);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.e("GoogleCaptchaActivity", "shouldOverrideUrlLoading 21: "+request.getUrl()+" : "+request.getMethod()+" : "+request.getRequestHeaders().size());
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("GoogleCaptchaActivity", "shouldOverrideUrlLoading 19: " + url);
            /*Disposable disposable = Observable
                    .fromCallable(() -> client.get(url, new HashMap<>()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            stringSingle -> {
                                onResponse();
                            },
                            throwable -> {
                                Toast.makeText(GoogleCaptchaActivity.this, "error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
            compositeDisposable.add(disposable);*/
            onResponse();
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void onResponse() {
        Toast.makeText(this, "Приложение будет перезапущено", Toast.LENGTH_SHORT).show();
        finish();
        /*new Handler().postDelayed(() -> {
            Activity activity = App.getActivity();
            if (activity == null) {
                Toast.makeText(App.getContext(), "Перезапустите приложение", Toast.LENGTH_SHORT).show();
            }
            MainActivity.restartApplication(activity);
        }, 1000);*/
    }
}
