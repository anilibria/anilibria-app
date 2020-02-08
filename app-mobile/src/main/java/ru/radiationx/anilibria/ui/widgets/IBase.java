package ru.radiationx.anilibria.ui.widgets;

import android.webkit.JavascriptInterface;

/**
 * Created by radiationx on 20.12.17.
 */

public interface IBase {
    String JS_BASE_INTERFACE = "IBase";

    //Событие DOMContentLoaded
    @JavascriptInterface
    void domContentLoaded();

    //Событие load в js
    @JavascriptInterface
    void onPageLoaded();
}
