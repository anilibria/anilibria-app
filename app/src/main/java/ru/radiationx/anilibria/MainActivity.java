package ru.radiationx.anilibria;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.radiationx.anilibria.api.Api;
import ru.radiationx.anilibria.api.releases.ReleaseItem;
import ru.radiationx.anilibria.api.releases.ReleaseParser;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ReleaseAdapter adapter;
    private int page = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoader(getApplicationContext());
        setContentView(R.layout.activity_main);
        refreshLayout = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_view);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                run(false);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        adapter = new ReleaseAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setListener(new ReleaseAdapter.ItemListener() {
            @Override
            public void onLoadMore() {
                page++;
                run(true);
            }

            @Override
            public void onItemClick(ReleaseItem item) {

            }

            @Override
            public boolean onItemLongClick(ReleaseItem item) {
                return false;
            }
        });
        run(false);
    }

    public void run(final boolean more) {
        Log.d("SUKA", "try load page " + page + ", with more " + more);
        if (!more) {
            refreshLayout.setRefreshing(true);
        }
        ReleaseParser.releaseItemsAsync(page, new Api.ApiCallback<ArrayList<ReleaseItem>>() {
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(final ArrayList<ReleaseItem> object) throws Exception {
                Log.d("SUKA", Thread.currentThread().toString());
                for (ReleaseItem item : object) {
                    Log.d("SUKA", "item id=" + item.getId() + "; title=" + item.getTitle());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                        if (more) {
                            adapter.insertMore(object);
                        } else {
                            adapter.addAll(object);
                        }
                    }
                });

            }
        });

    }

    private static DisplayImageOptions.Builder options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(new Handler())
            .displayer(new FadeInBitmapDisplayer(500, true, true, false));

    public static DisplayImageOptions.Builder getDefaultOptionsUIL() {
        return options;
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 5 Mb
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(options.build())
                .build();
        ImageLoader.getInstance().init(config);
    }
}
