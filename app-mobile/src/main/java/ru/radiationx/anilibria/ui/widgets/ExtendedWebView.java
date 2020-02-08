package ru.radiationx.anilibria.ui.widgets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.extension.ContextKt;

/**
 * Created by radiationx on 01.11.16.
 */

public class ExtendedWebView extends NestedWebView implements IBase {
    private final String LOG_TAG = ExtendedWebView.class.getSimpleName() + ": " + Integer.toHexString(System.identityHashCode(this));
    public final static int DIRECTION_NONE = 0;
    public final static int DIRECTION_UP = 1;
    public final static int DIRECTION_DOWN = 2;
    private int direction = DIRECTION_NONE;
    private boolean isJsReady = false;

    private OnDirectionListener onDirectionListener;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Thread mUiThread;
    private final Queue<Runnable> actionsForWebView = new LinkedList<>();
    private JsLifeCycleListener jsLifeCycleListener;

    public interface OnDirectionListener {
        void onDirectionChanged(int direction);
    }

    public ExtendedWebView(Context context) {
        super(context);
        init();
    }

    public ExtendedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExtendedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(LOG_TAG, "onPause " + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(LOG_TAG, "onResume " + this);
    }

    public void setOnDirectionListener(OnDirectionListener onDirectionListener) {
        this.onDirectionListener = onDirectionListener;
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
        int newDirection = scrollY > oldScrollY ? DIRECTION_DOWN : DIRECTION_UP;
        if (newDirection != direction) {
            direction = newDirection;
            if (onDirectionListener != null) {
                onDirectionListener.onDirectionChanged(newDirection);
            }
        }
    }

    public int getDirection() {
        return direction;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void init() {
        mUiThread = Thread.currentThread();
        addJavascriptInterface(this, IBase.JS_BASE_INTERFACE);
        WebSettings settings = getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setBuiltInZoomControls(false);
        settings.setDefaultFontSize(16);
        settings.setTextZoom(100);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //setRelativeFontSize(Preferences.Main.getWebViewSize(getContext()));
        //setBackgroundColor(App.getColorFromAttr(getContext(), R.attr.background_base));
        setBackgroundColor(ContextKt.getColorFromAttr(getContext(), R.attr.windowBackground));
    }

    public void easyLoadData(String baseUrl, String data) {
        Log.d("kekeke", "easyLoadData " + baseUrl);
        loadDataWithBaseURL(baseUrl, data, "text/html", "UTF-8", null);
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        isJsReady = false;
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        isJsReady = false;
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void loadUrl(String url) {
        isJsReady = false;
        super.loadUrl(url);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        isJsReady = false;
        super.loadUrl(url, additionalHttpHeaders);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestFocus();
        isJsReady = false;
        /*Log.e(LOG_TAG, "onAttachedToWindow");
        for (Runnable action : actionsForWebView) {
            mHandler.removeCallbacks(action);
        }
        actionsForWebView.clear();*/
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.e(LOG_TAG, "onDetachedFromWindow");
        isJsReady = false;
        for (Runnable action : actionsForWebView) {
            mHandler.removeCallbacks(action);
        }
        Log.e(LOG_TAG, "CLEAR ACTIONS detach");
        actionsForWebView.clear();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String script) {
        //Log.d("EWV", "evalJs: " + script);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                evalJs(script, null);
            } catch (Exception error) {
                error.printStackTrace();
                loadUrl("javascript:" + script);
            }
        } else {
            loadUrl("javascript:" + script);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String script, ValueCallback<String> resultCallback) {
        syncWithJs(() -> evaluateJavascript(script, resultCallback));
    }

    /*
     * JS LIFECYCLE
     * */

    @Override
    @JavascriptInterface
    public void domContentLoaded() {
        runInUiThread(() -> {
            Log.d(LOG_TAG, "domContentLoaded " + isJsReady + ", actions: " + actionsForWebView.size());
            isJsReady = true;
            for (Runnable action : actionsForWebView) {
                try {
                    runInUiThread(action);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            Log.e(LOG_TAG, "CLEAR ACTIONS loaded");
            actionsForWebView.clear();

            ArrayList<String> actions = new ArrayList<>();
            if (jsLifeCycleListener != null) {
                try {
                    jsLifeCycleListener.onDomContentComplete(actions);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            actions.add("nativeEvents.onNativeDomComplete();");
            evalJs(TextUtils.join("", actions));
        });
    }

    @Override
    @JavascriptInterface
    public void onPageLoaded() {
        runInUiThread(() -> {
            Log.d(LOG_TAG, "onPageLoaded " + isJsReady);
            ArrayList<String> actions = new ArrayList<>();
            if (jsLifeCycleListener != null) {
                try {
                    jsLifeCycleListener.onPageComplete(actions);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            actions.add("nativeEvents.onNativePageComplete();");

            StringBuilder script = new StringBuilder();
            for (String action : actions) {
                script.append(action);
            }
            evalJs(script.toString());
        });
    }

    public final void runInUiThread(final Runnable action) {
        //Log.d(LOG_TAG, "runInUiThread " + (Thread.currentThread() == mUiThread));
        if (Thread.currentThread() == mUiThread) {
            action.run();
        } else {
            mHandler.post(action);
        }
    }

    public void setJsLifeCycleListener(JsLifeCycleListener jsLifeCycleListener) {
        this.jsLifeCycleListener = jsLifeCycleListener;
    }

    public interface JsLifeCycleListener {
        void onDomContentComplete(final ArrayList<String> actions);

        void onPageComplete(final ArrayList<String> actions);
    }


    public void syncWithJs(final Runnable action) {
        Log.d(LOG_TAG, "syncWithJs " + isJsReady);
        if (!isJsReady) {
            Log.e(LOG_TAG, "ADD ACTION");
            actionsForWebView.add(action);
            Log.e(LOG_TAG, "ACTION ADDED: " + actionsForWebView.size());
        } else {
            try {
                runInUiThread(action);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }




    /*
     * OVERRIDE CONTEXT MENU
     * */

    private OnStartActionModeListener actionModeListener;

    public interface OnStartActionModeListener {
        void OnStart(ActionMode actionMode, ActionMode.Callback callback, int type);
    }

    public void setActionModeListener(OnStartActionModeListener actionModeListener) {
        this.actionModeListener = actionModeListener;
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        return myActionMode(callback, 0);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return myActionMode(callback, type);
    }

    private ActionMode myActionMode(ActionMode.Callback callback, int type) {
        ViewParent parent = getParent();
        if (parent == null) {
            return null;
        }
        ActionMode actionMode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            actionMode = super.startActionMode(callback, type);
        } else {
            actionMode = super.startActionMode(callback);
        }
        if (actionModeListener != null)
            actionModeListener.OnStart(actionMode, callback, type);
        return actionMode;
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        requestFocusNodeHref(new Handler(msg -> {
            HitTestResult result = getHitTestResult();
            //DialogsHelper.handleContextMenu(getContext(), result.getType(), result.getExtra(), (String) msg.getData().get("url"));
            return true;
        }).obtainMessage());
    }

    public void endWork() {
        setActionModeListener(null);
        setWebChromeClient(null);
        setWebViewClient(null);
        loadUrl("about:blank");
        clearHistory();
        clearSslPreferences();
        clearDisappearingChildren();
        clearFocus();
        clearFormData();
        clearMatches();
    }
}