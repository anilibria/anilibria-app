package ru.radiationx.anilibria.ui.fragments.release;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;

import ru.radiationx.anilibria.App;
import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.Screens;
import ru.radiationx.anilibria.data.api.releases.ReleaseItem;
import ru.radiationx.anilibria.ui.fragments.BaseFragment;
import ru.radiationx.anilibria.utils.Utils;

/**
 * Created by radiationx on 16.11.17.
 */

public class ReleaseFragment extends BaseFragment implements ReleaseView, ReleaseAdapter.ReleaseListener {
    public final static String ARG_ID = "release_id";
    public final static String ARG_ITEM = "release_item";
    public final static String ARG_TITLE = "release_id";
    public final static String ARG_TITLE_ORIG = "release_id";
    public final static String ARG_SEASONS = "release_id";
    public final static String ARG_VOICES = "release_id";
    public final static String ARG_TYPES = "release_id";
    public final static String ARG_GENRES = "release_id";
    public final static String ARG_DESC = "release_id";

    private RecyclerView recyclerView;
    private ReleaseAdapter adapter;
    private int id = -1;

    @InjectPresenter
    ReleasePresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("SUKA", "onCreate: " + this);
        Bundle arguments = getArguments();
        if (arguments != null) {
            id = arguments.getInt(ARG_ID, id);
            ReleaseItem item = (ReleaseItem) arguments.getSerializable(ARG_ITEM);
            if (item != null) {
                presenter.setCurrentData(item);
            }
            Log.e("SUKA", "ITEM " + item + " : " + (item == null ? "null" : item.getTitle()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("SUKA", "onDestroy: " + this);
    }

    @Override
    public void onCreateView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {
        baseInflateFragment(inflater, R.layout.fragment_release);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle(R.string.fragment_title_release);
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new ReleaseAdapter(getMvpDelegate(), "0");
        adapter.setReleaseListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        fixToolbarInsets();
        setMarqueeTitle();
        toolbar.setNavigationOnClickListener(v -> App.get().getRouter().backTo(Screens.RELEASES_LIST));
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow_back);
        toolbar.getMenu().add("Копировать ссылку")
                .setOnMenuItemClickListener(item -> {
                    presenter.onCopyLinkClick();
                    return false;
                });
        toolbar.getMenu().add("Поделиться")
                .setOnMenuItemClickListener(item -> {
                    presenter.onShareClick();
                    return false;
                });

        presenter.loadRelease(id);
    }


    @Override
    public void setRefreshing(boolean refreshing) {

    }

    @Override
    public void showRelease(ReleaseItem release) {
        toolbar.setTitle(String.format("%s / %s", release.getTitle(), release.getOriginalTitle()));
        adapter.setRelease(release);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void loadTorrent(String url) {
        Utils.externalLink(url);
    }

    @Override
    public void shareRelease(String text) {
        Utils.shareText(text);
    }

    @Override
    public void copyLink(String url) {
        Utils.externalLink(url);
    }

    @Override
    public void onClickSd(String url) {
        Utils.externalLink(url);
    }

    @Override
    public void onClickHd(String url) {
        Utils.externalLink(url);
    }

    @Override
    public void onClickTorrent(String url) {
        presenter.onTorrentClick();
    }

    @Override
    public void onClickTag(String text) {
        Toast.makeText(getContext(), "Временно не поддерживается", Toast.LENGTH_SHORT).show();
    }
}
