package ru.radiationx.anilibria.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import ru.radiationx.anilibria.App;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by isanechek on 30.07.16.
 */

public class Utils {
    public static boolean isMM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static String getFileNameFromUrl(String url) {
        String fileName = url;
        try {
            fileName = URLDecoder.decode(url, "CP1251");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int cut = fileName.lastIndexOf('/');
        if (cut != -1) {
            fileName = fileName.substring(cut + 1);
        }
        return fileName;
    }

    public static void copyToClipBoard(String s) {
        ClipboardManager clipboard = (ClipboardManager) App.get().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", s);
        clipboard.setPrimaryClip(clip);
    }

    public static String readFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) App.get().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipDescription description = clipboard.getPrimaryClipDescription();
            ClipData data = clipboard.getPrimaryClip();
            if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                return String.valueOf(data.getItemAt(0).getText());
        }
        return null;
    }

    public static void shareText(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        App.get().startActivity(Intent.createChooser(sendIntent, "Поделиться").addFlags(FLAG_ACTIVITY_NEW_TASK));
    }

    public static void externalLink(String url) {
        Log.e("SUKA", "externalLink "+url);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK);
        App.get().startActivity(Intent.createChooser(intent, "Открыть в").addFlags(FLAG_ACTIVITY_NEW_TASK));
    }

    public static void longLog(String msg) {
        int maxLogSize = 1000;
        for (int i = 0; i <= msg.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > msg.length() ? msg.length() : end;
            Log.v("LONG_LOG", msg.substring(start, end));
        }
    }
}
