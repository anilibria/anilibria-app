package ru.radiationx.anilibria.ui.widgets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import ru.radiationx.shared.ktx.android.ContextKt;
import timber.log.Timber;

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
        setBackgroundColor(ContextKt.getColorFromAttr(getContext(), android.R.attr.colorBackground));
    }

    public void easyLoadData(String baseUrl, String data) {
        loadDataWithBaseURL(baseUrl, data, "text/html", "UTF-8", null);
    }

    @Override
    public void loadData(@NonNull String data, String mimeType, String encoding) {
        isJsReady = false;
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, @NonNull String data, String mimeType, String encoding, String historyUrl) {
        isJsReady = false;
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void loadUrl(@NonNull String url) {
        isJsReady = false;
        super.loadUrl(url);
    }

    @Override
    public void loadUrl(@NonNull String url, @NonNull Map<String, String> additionalHttpHeaders) {
        isJsReady = false;
        super.loadUrl(url, additionalHttpHeaders);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestFocus();
        isJsReady = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isJsReady = false;
        for (Runnable action : actionsForWebView) {
            mHandler.removeCallbacks(action);
        }
        actionsForWebView.clear();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void evalJs(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                evalJs(script, null);
            } catch (Exception error) {
                Timber.e(error);
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
            isJsReady = true;
            for (Runnable action : actionsForWebView) {
                try {
                    runInUiThread(action);
                } catch (Exception exception) {
                    Timber.e(exception);
                }
            }
            actionsForWebView.clear();

            ArrayList<String> actions = new ArrayList<>();
            if (jsLifeCycleListener != null) {
                try {
                    jsLifeCycleListener.onDomContentComplete(actions);
                } catch (Exception exception) {
                    Timber.e(exception);
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
            ArrayList<String> actions = new ArrayList<>();
            if (jsLifeCycleListener != null) {
                try {
                    jsLifeCycleListener.onPageComplete(actions);
                } catch (Exception exception) {
                    Timber.e(exception);
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
        if (!isJsReady) {
            actionsForWebView.add(action);
        } else {
            try {
                runInUiThread(action);
            } catch (Exception ex) {
                Timber.e(ex);
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