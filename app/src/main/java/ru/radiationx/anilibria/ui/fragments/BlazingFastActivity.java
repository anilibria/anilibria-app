package ru.radiationx.anilibria.ui.fragments;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import io.reactivex.disposables.CompositeDisposable;
import ru.radiationx.anilibria.App;
import ru.radiationx.anilibria.model.data.holders.CookieHolder;
import ru.radiationx.anilibria.model.data.remote.IClient;

/**
 * Created by radiationx on 09.11.17.
 */

public class BlazingFastActivity extends FragmentActivity {
    private WebView webView;
    private String content = "";
    private String url = "";

    private IClient client = App.injections.getClient();
    private CookieHolder cookieHolder = App.injections.getCookieHolder();

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
        Log.e("BlazingFastActivity", "domain: " + domain);

        webView.loadDataWithBaseURL(domain, "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js\"></script><script>" + content + "</script>", "text/html", "utf-8", null);

    }

    class CaptchaWebViewClient extends WebViewClient {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Log.e("BlazingFastActivity", "shouldInterceptRequest 21: " + request.getUrl());
            return super.shouldInterceptRequest(view, request);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.e("BlazingFastActivity", "shouldOverrideUrlLoading 21: " + request.getUrl() + " : " + request.getMethod());
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String cookies = CookieManager.getInstance().getCookie(url);
            String[] cookiesArray = cookies.split(";");
            for (String cookie : cookiesArray) {
                String[] cookieObj = cookie.split("=");
                if (cookieObj[0].toLowerCase().contains("BLAZINGFAST-WEB-PROTECT".toLowerCase())) {
                    Log.e("BlazingFastActivity", "putCookie '" + cookieObj[0] + "' : '" + cookieObj[1] + "'");
                    cookieHolder.putCookie(url, cookieObj[0], cookieObj[1]);
                }
            }
            Log.e("BlazingFastActivity", "shouldOverrideUrlLoading 19: " + url + " : coockies= " + cookies);
            /*Disposable disposable = Observable
                    .fromCallable(() -> client.get(url, new HashMap<>()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            stringSingle -> {
                                onResponse();
                            },
                            throwable -> {
                                Toast.makeText(BlazingFastActivity.this, "error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
